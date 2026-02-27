package com.medicalstore.controller;

import com.medicalstore.dto.DailyReportData;
import com.medicalstore.dto.GstReportData;
import com.medicalstore.dto.MonthlyReportData;
import com.medicalstore.service.ReportService;
import com.medicalstore.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
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
}
