package com.medicalstore.controller;

import com.medicalstore.dto.DailyReportData;
import com.medicalstore.dto.GstReportData;
import com.medicalstore.dto.MonthlyReportData;
import com.medicalstore.service.ReportService;
import com.medicalstore.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Map;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'SHOPKEEPER')")
public class ReportController {

    private final SaleService saleService;
    private final ReportService reportService;

    @GetMapping
    public String reportsHome(Model model) {
        return "reports/index";
    }

    @GetMapping("/daily")
    public String dailyReport(@RequestParam(required = false) String date, Model model) {
        LocalDate reportDate = date != null ? LocalDate.parse(date) : LocalDate.now();

        LocalDateTime startOfDay = reportDate.atStartOfDay();
        LocalDateTime endOfDay = reportDate.atTime(23, 59, 59);

        model.addAttribute("sales", saleService.getSalesByDateRange(startOfDay, endOfDay));
        model.addAttribute("totalSales", saleService.getTotalSalesBetween(startOfDay, endOfDay));
        model.addAttribute("reportDate", reportDate);
        model.addAttribute("reportType", "Daily");

        return "reports/sales-report";
    }

    @GetMapping("/monthly")
    public String monthlyReport(@RequestParam(required = false) String month, Model model) {
        YearMonth yearMonth = month != null ? YearMonth.parse(month) : YearMonth.now();

        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        model.addAttribute("sales", saleService.getSalesByDateRange(startOfMonth, endOfMonth));
        model.addAttribute("totalSales", saleService.getTotalSalesBetween(startOfMonth, endOfMonth));
        model.addAttribute("reportMonth", yearMonth);
        model.addAttribute("reportType", "Monthly");

        return "reports/sales-report";
    }

    @GetMapping("/yearly")
    public String yearlyReport(@RequestParam(required = false) Integer year, Model model) {
        int reportYear = year != null ? year : LocalDate.now().getYear();

        LocalDateTime startOfYear = LocalDate.of(reportYear, 1, 1).atStartOfDay();
        LocalDateTime endOfYear = LocalDate.of(reportYear, 12, 31).atTime(23, 59, 59);

        model.addAttribute("sales", saleService.getSalesByDateRange(startOfYear, endOfYear));
        model.addAttribute("totalSales", saleService.getTotalSalesBetween(startOfYear, endOfYear));
        model.addAttribute("reportYear", reportYear);
        model.addAttribute("reportType", "Yearly");

        return "reports/sales-report";
    }

    @GetMapping("/daily-detailed")
    public String dailyDetailedReport(@RequestParam(required = false) String date, Model model) {
        LocalDate reportDate = date != null ? LocalDate.parse(date) : LocalDate.now();

        LocalDateTime startOfDay = reportDate.atStartOfDay();
        LocalDateTime endOfDay = reportDate.atTime(23, 59, 59);

        DailyReportData reportData = reportService.generateDailyReport(startOfDay, endOfDay);

        model.addAttribute("reportDate", reportDate);
        model.addAttribute("reportData", reportData);
        model.addAttribute("sales", saleService.getSalesByDateRange(startOfDay, endOfDay));
        model.addAttribute("reportGeneratedAt", LocalDateTime.now());

        return "reports/daily-detailed";
    }

    @GetMapping("/monthly-detailed")
    public String monthlyDetailedReport(@RequestParam(required = false) String month, Model model) {
        YearMonth yearMonth = month != null ? YearMonth.parse(month) : YearMonth.now();

        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        MonthlyReportData reportData = reportService.generateMonthlyReport(startOfMonth, endOfMonth, yearMonth);

        model.addAttribute("reportMonth", yearMonth);
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportGeneratedAt", LocalDateTime.now());

        return "reports/monthly-detailed";
    }

    @GetMapping("/gst")
    public String gstReport(@RequestParam(required = false) String month, Model model) {
        YearMonth yearMonth = month != null ? YearMonth.parse(month) : YearMonth.now();

        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        GstReportData reportData = reportService.generateGstReport(startOfMonth, endOfMonth);

        model.addAttribute("reportMonth", yearMonth);
        model.addAttribute("reportData", reportData);
        model.addAttribute("reportGeneratedAt", LocalDateTime.now());

        return "reports/gst-report";
    }

    @GetMapping("/expiry")
    public String expiryReport(Model model) {
        model.addAttribute("expired", reportService.getExpiredMedicines());
        model.addAttribute("expiring30", reportService.getExpiringMedicines(30));
        model.addAttribute("expiring60", reportService.getExpiringMedicines(60));
        model.addAttribute("expiring90", reportService.getExpiringMedicines(90));
        model.addAttribute("expiredValue", reportService.calculateStockValue(reportService.getExpiredMedicines()));
        model.addAttribute("expiring30Value",
                reportService.calculateStockValue(reportService.getExpiringMedicines(30)));
        model.addAttribute("expiring60Value",
                reportService.calculateStockValue(reportService.getExpiringMedicines(60)));
        model.addAttribute("expiring90Value",
                reportService.calculateStockValue(reportService.getExpiringMedicines(90)));
        return "reports/expiry-report";
    }

    @GetMapping("/profit-loss")
    public String profitLossReport(@RequestParam(required = false) String month, Model model) {
        YearMonth yearMonth = month != null ? YearMonth.parse(month) : YearMonth.now();

        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.atEndOfMonth().atTime(23, 59, 59);

        Map<String, Object> plData = reportService.generateProfitLossReport(start, end);
        model.addAttribute("plData", plData);
        model.addAttribute("reportMonth", yearMonth);

        // Last 6 months trend data for chart
        model.addAttribute("trendData", reportService.getMonthlyPLTrend(6));

        return "reports/profit-loss";
    }

    @GetMapping("/export/excel")
    public void exportSalesExcel(@RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            jakarta.servlet.http.HttpServletResponse response) throws Exception {
        LocalDateTime start = startDate != null ? LocalDate.parse(startDate).atStartOfDay()
                : LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime end = endDate != null ? LocalDate.parse(endDate).atTime(23, 59, 59)
                : LocalDate.now().atTime(23, 59, 59);

        byte[] excelBytes = reportService.exportSalesExcel(start, end);

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=sales-report.xlsx");
        response.getOutputStream().write(excelBytes);
        response.getOutputStream().flush();
    }
}
