package com.medicalstore.service;

import com.medicalstore.dto.DailyReportData;
import com.medicalstore.dto.GstReportData;
import com.medicalstore.dto.MonthlyReportData;
import com.medicalstore.model.Sale;
import com.medicalstore.repository.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final SaleRepository saleRepository;

    public DailyReportData generateDailyReport(LocalDateTime startOfDay, LocalDateTime endOfDay) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startOfDay, endOfDay);

        if (sales.isEmpty()) {
            return createEmptyDailyReport();
        }

        // Calculate basic metrics
        int totalTransactions = sales.size();
        double grossRevenue = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
        double totalDiscount = sales.stream()
                .mapToDouble(s -> s.getDiscountAmount() != null ? s.getDiscountAmount() : 0.0).sum();
        double totalGST = sales.stream().mapToDouble(s -> s.getGstAmount() != null ? s.getGstAmount() : 0.0).sum();

        // Calculate average percentages
        double avgDiscountPercentage = sales.stream()
                .filter(s -> s.getDiscountPercentage() != null && s.getDiscountPercentage() > 0)
                .mapToDouble(Sale::getDiscountPercentage)
                .average()
                .orElse(0.0);

        double avgGSTPercentage = sales.stream()
                .filter(s -> s.getGstPercentage() != null && s.getGstPercentage() > 0)
                .mapToDouble(Sale::getGstPercentage)
                .average()
                .orElse(0.0);

        double amountAfterDiscount = grossRevenue - totalDiscount;
        double netRevenue = amountAfterDiscount + totalGST;

        // Calculate item metrics
        int totalItemsSold = sales.stream().mapToInt(Sale::getQuantity).sum();
        double avgTransactionValue = netRevenue / totalTransactions;
        double avgItemsPerTransaction = (double) totalItemsSold / totalTransactions;

        // Count unique customers
        long uniqueCustomers = sales.stream()
                .filter(s -> s.getCustomer() != null)
                .map(s -> s.getCustomer().getId())
                .distinct()
                .count();

        // Payment breakdown
        List<PaymentBreakdown> paymentBreakdown = calculatePaymentBreakdown(sales);

        // Top medicines
        List<TopMedicine> topMedicines = calculateTopMedicines(sales, 10);

        return DailyReportData.builder()
                .totalTransactions(totalTransactions)
                .grossRevenue(grossRevenue)
                .totalDiscount(totalDiscount)
                .avgDiscountPercentage(avgDiscountPercentage)
                .amountAfterDiscount(amountAfterDiscount)
                .totalGST(totalGST)
                .avgGSTPercentage(avgGSTPercentage)
                .netRevenue(netRevenue)
                .totalItemsSold(totalItemsSold)
                .avgTransactionValue(avgTransactionValue)
                .avgItemsPerTransaction(avgItemsPerTransaction)
                .uniqueCustomers((int) uniqueCustomers)
                .paymentBreakdown(paymentBreakdown)
                .topMedicines(topMedicines)
                .build();
    }

    public MonthlyReportData generateMonthlyReport(LocalDateTime startOfMonth, LocalDateTime endOfMonth,
            YearMonth yearMonth) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(startOfMonth, endOfMonth);

        if (sales.isEmpty()) {
            return createEmptyMonthlyReport();
        }

        // Basic calculations
        int totalTransactions = sales.size();
        double grossRevenue = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
        double totalDiscount = sales.stream()
                .mapToDouble(s -> s.getDiscountAmount() != null ? s.getDiscountAmount() : 0.0).sum();
        double totalGST = sales.stream().mapToDouble(s -> s.getGstAmount() != null ? s.getGstAmount() : 0.0).sum();

        // Calculate average percentages
        double avgDiscountPercentage = sales.stream()
                .filter(s -> s.getDiscountPercentage() != null && s.getDiscountPercentage() > 0)
                .mapToDouble(Sale::getDiscountPercentage)
                .average()
                .orElse(0.0);

        int discountedTransactions = (int) sales.stream()
                .filter(s -> s.getDiscountPercentage() != null && s.getDiscountPercentage() > 0)
                .count();

        double avgGSTPercentage = sales.stream()
                .filter(s -> s.getGstPercentage() != null && s.getGstPercentage() > 0)
                .mapToDouble(Sale::getGstPercentage)
                .average()
                .orElse(0.0);

        double amountAfterDiscount = grossRevenue - totalDiscount;
        double totalRevenue = amountAfterDiscount + totalGST;

        // Calculate business days (days with transactions)
        Set<LocalDate> businessDays = sales.stream()
                .map(s -> s.getSaleDate().toLocalDate())
                .collect(Collectors.toSet());

        double avgDailyRevenue = totalRevenue / businessDays.size();

        // Customer metrics
        long totalCustomers = sales.stream()
                .filter(s -> s.getCustomer() != null)
                .map(s -> s.getCustomer().getId())
                .distinct()
                .count();

        // Item metrics
        int totalItemsSold = sales.stream().mapToInt(Sale::getQuantity).sum();
        double avgTransactionValue = totalRevenue / totalTransactions;
        double avgItemsPerTransaction = (double) totalItemsSold / totalTransactions;

        // Find peak day
        Map<LocalDate, Double> dailyRevenue = sales.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getSaleDate().toLocalDate(),
                        Collectors.summingDouble(
                                s -> s.getFinalAmount() != null ? s.getFinalAmount() : s.getTotalAmount())));

        Map.Entry<LocalDate, Double> peakDay = dailyRevenue.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .orElse(null);

        String peakDayStr = peakDay != null ? peakDay.getKey().format(DateTimeFormatter.ofPattern("dd MMM yyyy"))
                : "N/A";
        double peakDayRevenue = peakDay != null ? peakDay.getValue() : 0.0;

        // Percentage calculations
        double grossRevenuePercentage = (grossRevenue / totalRevenue) * 100;
        double discountPercentage = (totalDiscount / grossRevenue) * 100;
        double gstPercentage = (totalGST / totalRevenue) * 100;

        // Payment breakdown
        List<PaymentBreakdown> paymentBreakdown = calculateMonthlyPaymentBreakdown(sales, totalRevenue);

        // Top performers
        List<TopMedicine> topMedicines = calculateTopMedicines(sales, 10);
        List<TopCustomer> topCustomers = calculateTopCustomers(sales, 10);

        // Daily breakdown
        List<DailyBreakdown> dailyBreakdown = calculateDailyBreakdown(sales, yearMonth);

        return MonthlyReportData.builder()
                .totalTransactions(totalTransactions)
                .grossRevenue(grossRevenue)
                .totalDiscount(totalDiscount)
                .avgDiscountPercentage(avgDiscountPercentage)
                .discountedTransactions(discountedTransactions)
                .amountAfterDiscount(amountAfterDiscount)
                .totalGST(totalGST)
                .avgGSTPercentage(avgGSTPercentage)
                .totalRevenue(totalRevenue)
                .avgDailyRevenue(avgDailyRevenue)
                .totalCustomers((int) totalCustomers)
                .totalItemsSold(totalItemsSold)
                .avgTransactionValue(avgTransactionValue)
                .avgItemsPerTransaction(avgItemsPerTransaction)
                .businessDays(businessDays.size())
                .peakDay(peakDayStr)
                .peakDayRevenue(peakDayRevenue)
                .grossRevenuePercentage(grossRevenuePercentage)
                .discountPercentage(discountPercentage)
                .gstPercentage(gstPercentage)
                .paymentBreakdown(paymentBreakdown)
                .topMedicines(topMedicines)
                .topCustomers(topCustomers)
                .dailyBreakdown(dailyBreakdown)
                .build();
    }

    public GstReportData generateGstReport(LocalDateTime start, LocalDateTime end) {
        List<Sale> sales = saleRepository.findBySaleDateBetween(start, end);

        if (sales.isEmpty()) {
            return GstReportData.builder()
                    .totalTransactions(0)
                    .grossRevenue(0.0)
                    .totalDiscounts(0.0)
                    .totalTaxableAmount(0.0)
                    .totalGST(0.0)
                    .totalCGST(0.0)
                    .totalSGST(0.0)
                    .netRevenue(0.0)
                    .slabBreakdown(Collections.emptyList())
                    .dailyBreakdown(Collections.emptyList())
                    .build();
        }

        int totalTransactions = sales.size();
        double grossRevenue = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
        double totalDiscounts = sales.stream()
                .mapToDouble(s -> s.getDiscountAmount() != null ? s.getDiscountAmount() : 0.0).sum();
        double totalTaxableAmount = grossRevenue - totalDiscounts;
        double totalGST = sales.stream().mapToDouble(s -> s.getGstAmount() != null ? s.getGstAmount() : 0.0).sum();
        double totalCGST = totalGST / 2;
        double totalSGST = totalGST / 2;
        double netRevenue = totalTaxableAmount + totalGST;

        // GST slab-wise breakdown
        double[] slabs = { 0.0, 5.0, 12.0, 18.0, 28.0 };
        List<GstReportData.GstSlabBreakdown> slabBreakdown = new ArrayList<>();

        for (double slab : slabs) {
            List<Sale> slabSales = sales.stream()
                    .filter(s -> {
                        double gstPct = s.getGstPercentage() != null ? s.getGstPercentage() : 0.0;
                        return Math.abs(gstPct - slab) < 0.01;
                    })
                    .collect(Collectors.toList());

            if (!slabSales.isEmpty()) {
                double slabTaxable = slabSales.stream()
                        .mapToDouble(
                                s -> s.getTotalAmount() - (s.getDiscountAmount() != null ? s.getDiscountAmount() : 0.0))
                        .sum();
                double slabGst = slabSales.stream()
                        .mapToDouble(s -> s.getGstAmount() != null ? s.getGstAmount() : 0.0)
                        .sum();

                slabBreakdown.add(GstReportData.GstSlabBreakdown.builder()
                        .slabPercentage(slab)
                        .transactionCount(slabSales.size())
                        .taxableAmount(slabTaxable)
                        .cgstAmount(slabGst / 2)
                        .sgstAmount(slabGst / 2)
                        .totalGst(slabGst)
                        .build());
            }
        }

        // Daily GST breakdown
        Map<LocalDate, List<Sale>> salesByDate = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getSaleDate().toLocalDate()));

        List<GstReportData.DailyGstBreakdown> dailyBreakdown = salesByDate.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    List<Sale> daySales = entry.getValue();
                    double dayTaxable = daySales.stream()
                            .mapToDouble(s -> s.getTotalAmount()
                                    - (s.getDiscountAmount() != null ? s.getDiscountAmount() : 0.0))
                            .sum();
                    double dayGst = daySales.stream()
                            .mapToDouble(s -> s.getGstAmount() != null ? s.getGstAmount() : 0.0)
                            .sum();
                    return GstReportData.DailyGstBreakdown.builder()
                            .date(entry.getKey().format(DateTimeFormatter.ofPattern("dd MMM yyyy")))
                            .transactionCount(daySales.size())
                            .taxableAmount(dayTaxable)
                            .gstCollected(dayGst)
                            .netRevenue(dayTaxable + dayGst)
                            .build();
                })
                .collect(Collectors.toList());

        return GstReportData.builder()
                .totalTransactions(totalTransactions)
                .grossRevenue(grossRevenue)
                .totalDiscounts(totalDiscounts)
                .totalTaxableAmount(totalTaxableAmount)
                .totalGST(totalGST)
                .totalCGST(totalCGST)
                .totalSGST(totalSGST)
                .netRevenue(netRevenue)
                .slabBreakdown(slabBreakdown)
                .dailyBreakdown(dailyBreakdown)
                .build();
    }

    private List<PaymentBreakdown> calculatePaymentBreakdown(List<Sale> sales) {
        Map<String, String> paymentColors = Map.of(
                "Cash", "#28a745",
                "Card", "#007bff",
                "UPI", "#6f42c1",
                "Other", "#6c757d");

        Map<String, List<Sale>> groupedByPayment = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getPaymentMethod() != null ? s.getPaymentMethod() : "Cash"));

        return groupedByPayment.entrySet().stream()
                .map(entry -> new PaymentBreakdown(
                        entry.getKey(),
                        entry.getValue().size(),
                        entry.getValue().stream()
                                .mapToDouble(s -> s.getFinalAmount() != null ? s.getFinalAmount() : s.getTotalAmount())
                                .sum(),
                        paymentColors.getOrDefault(entry.getKey(), "#6c757d")))
                .sorted(Comparator.comparing(PaymentBreakdown::getAmount).reversed())
                .collect(Collectors.toList());
    }

    private List<PaymentBreakdown> calculateMonthlyPaymentBreakdown(List<Sale> sales, double totalRevenue) {
        Map<String, String> paymentColors = Map.of(
                "Cash", "#28a745",
                "Card", "#007bff",
                "UPI", "#6f42c1",
                "Other", "#6c757d");

        Map<String, List<Sale>> groupedByPayment = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getPaymentMethod() != null ? s.getPaymentMethod() : "Cash"));

        return groupedByPayment.entrySet().stream()
                .map(entry -> {
                    double amount = entry.getValue().stream()
                            .mapToDouble(s -> s.getFinalAmount() != null ? s.getFinalAmount() : s.getTotalAmount())
                            .sum();
                    return new PaymentBreakdown(
                            entry.getKey(),
                            entry.getValue().size(),
                            amount,
                            (amount / totalRevenue) * 100,
                            paymentColors.getOrDefault(entry.getKey(), "#6c757d"));
                })
                .sorted(Comparator.comparing(PaymentBreakdown::getAmount).reversed())
                .collect(Collectors.toList());
    }

    private List<TopMedicine> calculateTopMedicines(List<Sale> sales, int limit) {
        Map<String, List<Sale>> groupedByMedicine = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getMedicine().getName()));

        return groupedByMedicine.entrySet().stream()
                .map(entry -> {
                    List<Sale> medicineSales = entry.getValue();
                    return new TopMedicine(
                            entry.getKey(),
                            medicineSales.get(0).getMedicine().getCategory(),
                            medicineSales.stream().mapToInt(Sale::getQuantity).sum(),
                            medicineSales.stream()
                                    .mapToDouble(
                                            s -> s.getFinalAmount() != null ? s.getFinalAmount() : s.getTotalAmount())
                                    .sum());
                })
                .sorted(Comparator.comparing(TopMedicine::getRevenue).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<TopCustomer> calculateTopCustomers(List<Sale> sales, int limit) {
        Map<String, List<Sale>> groupedByCustomer = sales.stream()
                .filter(s -> s.getCustomer() != null)
                .collect(Collectors.groupingBy(s -> s.getCustomer().getName()));

        return groupedByCustomer.entrySet().stream()
                .map(entry -> {
                    List<Sale> customerSales = entry.getValue();
                    return new TopCustomer(
                            entry.getKey(),
                            customerSales.size(),
                            customerSales.stream()
                                    .mapToDouble(
                                            s -> s.getFinalAmount() != null ? s.getFinalAmount() : s.getTotalAmount())
                                    .sum());
                })
                .sorted(Comparator.comparing(TopCustomer::getTotalSpent).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    private List<DailyBreakdown> calculateDailyBreakdown(List<Sale> sales, YearMonth yearMonth) {
        Map<LocalDate, List<Sale>> salesByDate = sales.stream()
                .collect(Collectors.groupingBy(s -> s.getSaleDate().toLocalDate()));

        List<DailyBreakdown> breakdown = new ArrayList<>();

        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            List<Sale> daySales = salesByDate.getOrDefault(date, Collections.emptyList());

            if (!daySales.isEmpty()) {
                double grossRevenue = daySales.stream().mapToDouble(Sale::getTotalAmount).sum();
                double discounts = daySales.stream()
                        .mapToDouble(s -> s.getDiscountAmount() != null ? s.getDiscountAmount() : 0.0)
                        .sum();
                double gst = daySales.stream()
                        .mapToDouble(s -> s.getGstAmount() != null ? s.getGstAmount() : 0.0)
                        .sum();
                double netRevenue = daySales.stream()
                        .mapToDouble(s -> s.getFinalAmount() != null ? s.getFinalAmount() : s.getTotalAmount())
                        .sum();

                breakdown.add(new DailyBreakdown(
                        date,
                        daySales.size(),
                        grossRevenue,
                        discounts,
                        gst,
                        netRevenue));
            }
        }

        return breakdown;
    }

    private DailyReportData createEmptyDailyReport() {
        return DailyReportData.builder()
                .totalTransactions(0)
                .grossRevenue(0.0)
                .totalDiscount(0.0)
                .avgDiscountPercentage(0.0)
                .amountAfterDiscount(0.0)
                .totalGST(0.0)
                .avgGSTPercentage(0.0)
                .netRevenue(0.0)
                .totalItemsSold(0)
                .avgTransactionValue(0.0)
                .avgItemsPerTransaction(0.0)
                .uniqueCustomers(0)
                .paymentBreakdown(Collections.emptyList())
                .topMedicines(Collections.emptyList())
                .build();
    }

    private MonthlyReportData createEmptyMonthlyReport() {
        return MonthlyReportData.builder()
                .totalTransactions(0)
                .grossRevenue(0.0)
                .totalDiscount(0.0)
                .avgDiscountPercentage(0.0)
                .discountedTransactions(0)
                .amountAfterDiscount(0.0)
                .totalGST(0.0)
                .avgGSTPercentage(0.0)
                .totalRevenue(0.0)
                .avgDailyRevenue(0.0)
                .totalCustomers(0)
                .totalItemsSold(0)
                .avgTransactionValue(0.0)
                .avgItemsPerTransaction(0.0)
                .businessDays(0)
                .peakDay("N/A")
                .peakDayRevenue(0.0)
                .grossRevenuePercentage(0.0)
                .discountPercentage(0.0)
                .gstPercentage(0.0)
                .paymentBreakdown(Collections.emptyList())
                .topMedicines(Collections.emptyList())
                .topCustomers(Collections.emptyList())
                .dailyBreakdown(Collections.emptyList())
                .build();
    }

    // Inner classes for breakdown data
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PaymentBreakdown {
        private String method;
        private Integer count;
        private Double amount;
        private Double percentage;
        private String color;

        public PaymentBreakdown(String method, Integer count, Double amount, String color) {
            this.method = method;
            this.count = count;
            this.amount = amount;
            this.color = color;
        }
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TopMedicine {
        private String name;
        private String category;
        private Integer quantitySold;
        private Double revenue;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TopCustomer {
        private String name;
        private Integer purchaseCount;
        private Double totalSpent;
    }

    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class DailyBreakdown {
        private LocalDate date;
        private Integer transactions;
        private Double grossRevenue;
        private Double discounts;
        private Double gst;
        private Double netRevenue;
    }
}
