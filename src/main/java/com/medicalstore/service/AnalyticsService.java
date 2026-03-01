package com.medicalstore.service;

import com.medicalstore.model.Medicine;
import com.medicalstore.repository.MedicineRepository;
import com.medicalstore.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AnalyticsService {

    private final SaleRepository saleRepository;
    private final MedicineRepository medicineRepository;

    // ═══════════════════════════════════════════════════════════════════
    // 1. Profit per Medicine
    // ═══════════════════════════════════════════════════════════════════

    /**
     * Returns list of maps with keys: id, name, category, revenue, cost, profit,
     * qtySold, margin
     */
    @Cacheable(value = "analytics_profit", key = "#start.toLocalDate().toString() + '-' + #end.toLocalDate().toString()")
    public List<Map<String, Object>> getProfitPerMedicine(LocalDateTime start, LocalDateTime end) {
        List<Object[]> rows = saleRepository.getProfitPerMedicine(start, end);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row[0]);
            item.put("name", row[1]);
            item.put("category", row[2]);
            double revenue = row[3] != null ? ((Number) row[3]).doubleValue() : 0;
            double cost = row[4] != null ? ((Number) row[4]).doubleValue() : 0;
            double profit = row[5] != null ? ((Number) row[5]).doubleValue() : 0;
            long qtySold = row[6] != null ? ((Number) row[6]).longValue() : 0;
            double margin = revenue > 0 ? (profit / revenue) * 100 : 0;

            item.put("revenue", revenue);
            item.put("cost", cost);
            item.put("profit", profit);
            item.put("qtySold", qtySold);
            item.put("margin", margin);
            result.add(item);
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════
    // 2. Dead Stock (> N days with no sale)
    // ═══════════════════════════════════════════════════════════════════

    @Cacheable(value = "analytics_deadstock", key = "#days")
    public List<Map<String, Object>> getDeadStock(int days) {
        LocalDateTime since = LocalDate.now().minusDays(days).atStartOfDay();
        List<Long> activeIds = saleRepository.getMedicineIdsWithSalesSince(since);

        List<Medicine> deadMedicines;
        if (activeIds == null || activeIds.isEmpty()) {
            // No sales at all — all in-stock medicines are "dead"
            deadMedicines = medicineRepository.findByQuantityGreaterThan(0);
        } else {
            deadMedicines = medicineRepository.findDeadStock(activeIds);
        }

        return deadMedicines.stream().map(m -> {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("name", m.getName());
            item.put("category", m.getCategory());
            item.put("quantity", m.getQuantity());
            double stockValue = (m.getPrice() != null ? m.getPrice() : 0)
                    * (m.getQuantity() != null ? m.getQuantity() : 0);
            item.put("stockValue", stockValue);
            item.put("purchasePrice", m.getPurchasePrice() != null ? m.getPurchasePrice() : 0);
            item.put("sellingPrice", m.getPrice() != null ? m.getPrice() : 0);
            item.put("expiryDate", m.getExpiryDate());
            item.put("batchNumber", m.getBatchNumber());
            return item;
        }).collect(Collectors.toList());
    }

    // ═══════════════════════════════════════════════════════════════════
    // 3. Fast-Moving Items (Top N)
    // ═══════════════════════════════════════════════════════════════════

    @Cacheable(value = "analytics_fastmoving", key = "#limit + '-' + #start.toLocalDate().toString() + '-' + #end.toLocalDate().toString()")
    public List<Map<String, Object>> getFastMovingItems(int limit, LocalDateTime start, LocalDateTime end) {
        // LIMIT pushed to SQL via Pageable — avoids loading all rows and breaking in Java
        List<Object[]> rows = saleRepository.getTopSellingMedicinesLimited(start, end, PageRequest.of(0, limit));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", row[0]);
            item.put("name", row[1]);
            item.put("category", row[2]);
            item.put("qtySold", row[3] != null ? ((Number) row[3]).longValue() : 0);
            item.put("revenue", row[4] != null ? ((Number) row[4]).doubleValue() : 0);
            result.add(item);
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════
    // 4. GST Monthly Summary
    // ═══════════════════════════════════════════════════════════════════

    @Cacheable(value = "analytics_gst", key = "#year")
    public List<Map<String, Object>> getGstMonthlySummary(int year) {
        LocalDateTime start = LocalDate.of(year, 1, 1).atStartOfDay();
        LocalDateTime end = LocalDate.of(year, 12, 31).atTime(23, 59, 59);

        List<Object[]> rows = saleRepository.getMonthlyGstSummary(start, end);
        List<Map<String, Object>> result = new ArrayList<>();

        for (Object[] row : rows) {
            Map<String, Object> item = new LinkedHashMap<>();
            String ym = (String) row[0]; // "2026-02"
            try {
                YearMonth yearMonth = YearMonth.parse(ym, DateTimeFormatter.ofPattern("yyyy-MM"));
                item.put("month", yearMonth.format(DateTimeFormatter.ofPattern("MMM yyyy")));
                item.put("monthKey", ym);
            } catch (Exception e) {
                item.put("month", ym);
                item.put("monthKey", ym);
            }
            item.put("taxableAmount", row[1] != null ? ((Number) row[1]).doubleValue() : 0);
            item.put("cgst", row[2] != null ? ((Number) row[2]).doubleValue() : 0);
            item.put("sgst", row[3] != null ? ((Number) row[3]).doubleValue() : 0);
            item.put("totalGst", row[4] != null ? ((Number) row[4]).doubleValue() : 0);
            item.put("transactionCount", row[5] != null ? ((Number) row[5]).longValue() : 0);
            result.add(item);
        }
        return result;
    }

    // ═══════════════════════════════════════════════════════════════════
    // JSON builders for Chart.js
    // ═══════════════════════════════════════════════════════════════════

    public String buildProfitChartJson(List<Map<String, Object>> data, int limit) {
        List<Map<String, Object>> top = data.stream().limit(limit).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sb.append("{\"labels\":[");
        sb.append(top.stream().map(m -> "\"" + m.get("name") + "\"").collect(Collectors.joining(",")));
        sb.append("],\"profit\":[");
        sb.append(top.stream().map(m -> String.format("%.2f", (double) m.get("profit")))
                .collect(Collectors.joining(",")));
        sb.append("],\"revenue\":[");
        sb.append(top.stream().map(m -> String.format("%.2f", (double) m.get("revenue")))
                .collect(Collectors.joining(",")));
        sb.append("],\"cost\":[");
        sb.append(
                top.stream().map(m -> String.format("%.2f", (double) m.get("cost"))).collect(Collectors.joining(",")));
        sb.append("]}");
        return sb.toString();
    }

    public String buildFastMovingChartJson(List<Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"labels\":[");
        sb.append(data.stream().map(m -> "\"" + m.get("name") + "\"").collect(Collectors.joining(",")));
        sb.append("],\"qty\":[");
        sb.append(data.stream().map(m -> String.valueOf(m.get("qtySold"))).collect(Collectors.joining(",")));
        sb.append("],\"revenue\":[");
        sb.append(data.stream().map(m -> String.format("%.2f", (double) m.get("revenue")))
                .collect(Collectors.joining(",")));
        sb.append("]}");
        return sb.toString();
    }

    public String buildGstChartJson(List<Map<String, Object>> data) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\"labels\":[");
        sb.append(data.stream().map(m -> "\"" + m.get("month") + "\"").collect(Collectors.joining(",")));
        sb.append("],\"cgst\":[");
        sb.append(
                data.stream().map(m -> String.format("%.2f", (double) m.get("cgst"))).collect(Collectors.joining(",")));
        sb.append("],\"sgst\":[");
        sb.append(
                data.stream().map(m -> String.format("%.2f", (double) m.get("sgst"))).collect(Collectors.joining(",")));
        sb.append("],\"total\":[");
        sb.append(data.stream().map(m -> String.format("%.2f", (double) m.get("totalGst")))
                .collect(Collectors.joining(",")));
        sb.append("]}");
        return sb.toString();
    }

    // ═══════════════════════════════════════════════════════════════════
    // Excel Export
    // ═══════════════════════════════════════════════════════════════════

    public byte[] exportProfitPerMedicineExcel(List<Map<String, Object>> data) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Profit Per Medicine");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = { "#", "Medicine", "Category", "Qty Sold", "Revenue", "Cost", "Profit", "Margin %" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Map<String, Object> item : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue((String) item.get("name"));
                row.createCell(2).setCellValue((String) item.get("category"));
                row.createCell(3).setCellValue(((Number) item.get("qtySold")).longValue());
                row.createCell(4).setCellValue((Double) item.get("revenue"));
                row.createCell(5).setCellValue((Double) item.get("cost"));
                row.createCell(6).setCellValue((Double) item.get("profit"));
                row.createCell(7).setCellValue(String.format("%.1f%%", (Double) item.get("margin")));
            }

            for (int i = 0; i < headers.length; i++)
                sheet.autoSizeColumn(i);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    public byte[] exportDeadStockExcel(List<Map<String, Object>> data) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Dead Stock");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = { "#", "Medicine", "Category", "Quantity", "Purchase Price", "Selling Price",
                    "Stock Value", "Batch", "Expiry Date" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Map<String, Object> item : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue((String) item.get("name"));
                row.createCell(2).setCellValue((String) item.get("category"));
                row.createCell(3).setCellValue(((Number) item.get("quantity")).intValue());
                row.createCell(4).setCellValue(((Number) item.get("purchasePrice")).doubleValue());
                row.createCell(5).setCellValue(((Number) item.get("sellingPrice")).doubleValue());
                row.createCell(6).setCellValue(((Number) item.get("stockValue")).doubleValue());
                row.createCell(7)
                        .setCellValue(item.get("batchNumber") != null ? item.get("batchNumber").toString() : "");
                row.createCell(8).setCellValue(item.get("expiryDate") != null ? item.get("expiryDate").toString() : "");
            }

            for (int i = 0; i < headers.length; i++)
                sheet.autoSizeColumn(i);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    public byte[] exportFastMovingExcel(List<Map<String, Object>> data) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Fast Moving Items");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = { "Rank", "Medicine", "Category", "Qty Sold", "Revenue" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Map<String, Object> item : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue(rowNum - 1);
                row.createCell(1).setCellValue((String) item.get("name"));
                row.createCell(2).setCellValue((String) item.get("category"));
                row.createCell(3).setCellValue(((Number) item.get("qtySold")).longValue());
                row.createCell(4).setCellValue(((Number) item.get("revenue")).doubleValue());
            }

            for (int i = 0; i < headers.length; i++)
                sheet.autoSizeColumn(i);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }

    public byte[] exportGstSummaryExcel(List<Map<String, Object>> data) throws Exception {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("GST Monthly Summary");
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(0);
            String[] headers = { "Month", "Taxable Amount", "CGST", "SGST", "Total GST", "Transactions" };
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowNum = 1;
            for (Map<String, Object> item : data) {
                Row row = sheet.createRow(rowNum++);
                row.createCell(0).setCellValue((String) item.get("month"));
                row.createCell(1).setCellValue(((Number) item.get("taxableAmount")).doubleValue());
                row.createCell(2).setCellValue(((Number) item.get("cgst")).doubleValue());
                row.createCell(3).setCellValue(((Number) item.get("sgst")).doubleValue());
                row.createCell(4).setCellValue(((Number) item.get("totalGst")).doubleValue());
                row.createCell(5).setCellValue(((Number) item.get("transactionCount")).longValue());
            }

            for (int i = 0; i < headers.length; i++)
                sheet.autoSizeColumn(i);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            workbook.write(bos);
            return bos.toByteArray();
        }
    }
}
