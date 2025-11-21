package com.medicalstore.dto;

import com.medicalstore.service.ReportService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportData {
    private Integer totalTransactions;
    private Double grossRevenue;
    private Double totalDiscount;
    private Double avgDiscountPercentage;
    private Integer discountedTransactions;
    private Double amountAfterDiscount;
    private Double totalGST;
    private Double avgGSTPercentage;
    private Double totalRevenue;
    
    private Double avgDailyRevenue;
    private Integer totalCustomers;
    private Integer totalItemsSold;
    private Double avgTransactionValue;
    private Double avgItemsPerTransaction;
    private Integer businessDays;
    
    private String peakDay;
    private Double peakDayRevenue;
    
    private Double grossRevenuePercentage;
    private Double discountPercentage;
    private Double gstPercentage;
    
    private List<ReportService.PaymentBreakdown> paymentBreakdown;
    private List<ReportService.TopMedicine> topMedicines;
    private List<ReportService.TopCustomer> topCustomers;
    private List<ReportService.DailyBreakdown> dailyBreakdown;
}
