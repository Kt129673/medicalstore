package com.medicalstore.controller;

import com.medicalstore.model.Sale;
import com.medicalstore.service.PdfService;
import com.medicalstore.service.SaleService;
import com.medicalstore.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

@Controller
@RequestMapping("/pdf")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
public class PdfController {

    private final PdfService pdfService;
    private final SaleService saleService;
    private final ReportService reportService;

    /**
     * Generate and download invoice PDF
     */
    @GetMapping("/invoice/{id}")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        try {
            Sale sale = saleService.getSaleById(id)
                    .orElseThrow(() -> new RuntimeException("Sale not found with ID: " + id));

            byte[] pdfBytes = pdfService.generateInvoicePdf(sale);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    String.format("Invoice_INV-%06d.pdf", sale.getId()));
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Generate and download daily report PDF
     */
    @GetMapping("/report/daily")
    public ResponseEntity<byte[]> downloadDailyReportPdf(
            @RequestParam(required = false) String date) {
        try {
            LocalDate reportDate = date != null ? 
                    LocalDate.parse(date, DateTimeFormatter.ISO_DATE) : 
                    LocalDate.now();

            LocalDateTime startOfDay = reportDate.atStartOfDay();
            LocalDateTime endOfDay = reportDate.atTime(23, 59, 59);

            Object reportData = reportService.generateDailyReport(startOfDay, endOfDay);
            String dateStr = reportDate.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            
            byte[] pdfBytes = pdfService.generateDailyReportPdf(dateStr, reportData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    String.format("Daily_Report_%s.pdf", reportDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Generate and download monthly report PDF
     */
    @GetMapping("/report/monthly")
    public ResponseEntity<byte[]> downloadMonthlyReportPdf(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        try {
            LocalDate now = LocalDate.now();
            int reportMonth = month != null ? month : now.getMonthValue();
            int reportYear = year != null ? year : now.getYear();

            YearMonth yearMonth = YearMonth.of(reportYear, reportMonth);
            LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
            LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

            Object reportData = reportService.generateMonthlyReport(startOfMonth, endOfMonth, yearMonth);
            
            String monthName = yearMonth.format(DateTimeFormatter.ofPattern("MMMM"));
            
            byte[] pdfBytes = pdfService.generateMonthlyReportPdf(monthName, String.valueOf(reportYear), reportData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    String.format("Monthly_Report_%s_%d.pdf", monthName, reportYear));
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }

    /**
     * Generate and download custom sales report PDF
     */
    @GetMapping("/report/sales")
    public ResponseEntity<byte[]> downloadSalesReportPdf(
            @RequestParam String type,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        try {
            // Get report data based on type
            Object reportData;
            String start, end;
            
            switch (type.toLowerCase()) {
                case "daily":
                    LocalDate daily = (startDate != null && !startDate.trim().isEmpty()) ? 
                            LocalDate.parse(startDate) : LocalDate.now();
                    LocalDateTime dailyStart = daily.atStartOfDay();
                    LocalDateTime dailyEnd = daily.atTime(23, 59, 59);
                    reportData = saleService.getSalesByDateRange(dailyStart, dailyEnd);
                    start = end = daily.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
                    break;
                    
                case "monthly":
                    LocalDate monthly = (startDate != null && !startDate.trim().isEmpty()) ? 
                            LocalDate.parse(startDate) : LocalDate.now();
                    YearMonth ym = YearMonth.of(monthly.getYear(), monthly.getMonthValue());
                    LocalDateTime monthStart = ym.atDay(1).atStartOfDay();
                    LocalDateTime monthEnd = ym.atEndOfMonth().atTime(23, 59, 59);
                    reportData = saleService.getSalesByDateRange(monthStart, monthEnd);
                    start = monthly.format(DateTimeFormatter.ofPattern("MMMM yyyy"));
                    end = start;
                    break;
                    
                case "yearly":
                    int year = (startDate != null && !startDate.trim().isEmpty()) ? 
                            Integer.parseInt(startDate.substring(0, 4)) : LocalDate.now().getYear();
                    LocalDateTime yearStart = LocalDate.of(year, 1, 1).atStartOfDay();
                    LocalDateTime yearEnd = LocalDate.of(year, 12, 31).atTime(23, 59, 59);
                    reportData = saleService.getSalesByDateRange(yearStart, yearEnd);
                    start = "January " + year;
                    end = "December " + year;
                    break;
                    
                default:
                    throw new IllegalArgumentException("Invalid report type: " + type);
            }
            
            byte[] pdfBytes = pdfService.generateSalesReportPdf(
                    type.toUpperCase(), start, end, reportData);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment", 
                    String.format("%s_Sales_Report.pdf", type));
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(("Error generating PDF: " + e.getMessage()).getBytes());
        }
    }
}
