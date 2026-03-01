package com.medicalstore.controller;

import com.medicalstore.service.AnalyticsService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/analytics")
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER')")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public String index(Model model) {
        return "analytics/index";
    }

    @GetMapping("/profit-per-medicine")
    public String profitPerMedicine(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {

        LocalDateTime start = startDate != null ? LocalDate.parse(startDate).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = endDate != null ? LocalDate.parse(endDate).atTime(23, 59, 59)
                : LocalDate.now().atTime(23, 59, 59);

        List<Map<String, Object>> data = analyticsService.getProfitPerMedicine(start, end);

        model.addAttribute("data", data);
        model.addAttribute("chartJson", analyticsService.buildProfitChartJson(data, 15));
        model.addAttribute("startDate", start.toLocalDate());
        model.addAttribute("endDate", end.toLocalDate());

        return "analytics/profit-per-medicine";
    }

    @GetMapping("/dead-stock")
    public String deadStock(@RequestParam(required = false, defaultValue = "90") int days, Model model) {
        List<Map<String, Object>> data = analyticsService.getDeadStock(days);

        double totalStockValue = data.stream()
                .mapToDouble(item -> ((Number) item.get("stockValue")).doubleValue())
                .sum();

        model.addAttribute("data", data);
        model.addAttribute("days", days);
        model.addAttribute("totalStockValue", totalStockValue);
        model.addAttribute("itemCount", data.size());

        return "analytics/dead-stock";
    }

    @GetMapping("/fast-moving")
    public String fastMoving(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false, defaultValue = "20") int limit,
            Model model) {

        LocalDateTime start = startDate != null ? LocalDate.parse(startDate).atStartOfDay()
                : LocalDate.now().minusDays(30).atStartOfDay();
        LocalDateTime end = endDate != null ? LocalDate.parse(endDate).atTime(23, 59, 59)
                : LocalDate.now().atTime(23, 59, 59);

        List<Map<String, Object>> data = analyticsService.getFastMovingItems(limit, start, end);

        model.addAttribute("data", data);
        model.addAttribute("chartJson", analyticsService.buildFastMovingChartJson(data));
        model.addAttribute("startDate", start.toLocalDate());
        model.addAttribute("endDate", end.toLocalDate());
        model.addAttribute("limit", limit);

        return "analytics/fast-moving";
    }

    @GetMapping("/gst-summary")
    public String gstSummary(
            @RequestParam(required = false) Integer year,
            Model model) {

        int reportYear = year != null ? year : LocalDate.now().getYear();
        List<Map<String, Object>> data = analyticsService.getGstMonthlySummary(reportYear);

        double annualGst = data.stream().mapToDouble(d -> ((Number) d.get("totalGst")).doubleValue()).sum();
        double annualTaxable = data.stream().mapToDouble(d -> ((Number) d.get("taxableAmount")).doubleValue()).sum();

        model.addAttribute("data", data);
        model.addAttribute("chartJson", analyticsService.buildGstChartJson(data));
        model.addAttribute("year", reportYear);
        model.addAttribute("annualGst", annualGst);
        model.addAttribute("annualTaxable", annualTaxable);

        return "analytics/gst-summary";
    }

    // ═══════════════════════════════════════════════════════════════════
    // Export Endpoints
    // ═══════════════════════════════════════════════════════════════════

    @GetMapping("/export/excel")
    public void exportExcel(
            @RequestParam String report,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Integer days,
            @RequestParam(required = false) Integer limit,
            @RequestParam(required = false) Integer year,
            HttpServletResponse response) throws Exception {

        LocalDateTime start = startDate != null ? LocalDate.parse(startDate).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = endDate != null ? LocalDate.parse(endDate).atTime(23, 59, 59)
                : LocalDate.now().atTime(23, 59, 59);

        byte[] excelBytes = null;
        String filename = "analytics-report.xlsx";

        switch (report) {
            case "profit":
                excelBytes = analyticsService
                        .exportProfitPerMedicineExcel(analyticsService.getProfitPerMedicine(start, end));
                filename = "profit-per-medicine.xlsx";
                break;
            case "dead-stock":
                excelBytes = analyticsService
                        .exportDeadStockExcel(analyticsService.getDeadStock(days != null ? days : 90));
                filename = "dead-stock.xlsx";
                break;
            case "fast-moving":
                start = startDate != null ? LocalDate.parse(startDate).atStartOfDay()
                        : LocalDate.now().minusDays(30).atStartOfDay();
                excelBytes = analyticsService.exportFastMovingExcel(
                        analyticsService.getFastMovingItems(limit != null ? limit : 20, start, end));
                filename = "fast-moving-items.xlsx";
                break;
            case "gst":
                excelBytes = analyticsService.exportGstSummaryExcel(
                        analyticsService.getGstMonthlySummary(year != null ? year : LocalDate.now().getYear()));
                filename = "gst-monthly-summary.xlsx";
                break;
        }

        if (excelBytes != null) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);
            response.getOutputStream().write(excelBytes);
            response.getOutputStream().flush();
        }
    }
}
