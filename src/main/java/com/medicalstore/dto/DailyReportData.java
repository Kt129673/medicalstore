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
public class DailyReportData {
    private Integer totalTransactions;
    private Double grossRevenue;
    private Double totalDiscount;
    private Double avgDiscountPercentage;
    private Double amountAfterDiscount;
    private Double totalGST;
    private Double avgGSTPercentage;
    private Double netRevenue;
    
    private Integer totalItemsSold;
    private Double avgTransactionValue;
    private Double avgItemsPerTransaction;
    private Integer uniqueCustomers;
    
    private List<ReportService.PaymentBreakdown> paymentBreakdown;
    private List<ReportService.TopMedicine> topMedicines;
}
