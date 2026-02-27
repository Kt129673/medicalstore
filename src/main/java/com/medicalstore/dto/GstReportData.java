package com.medicalstore.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GstReportData {
    // Summary
    private Integer totalTransactions;
    private Double grossRevenue;
    private Double totalDiscounts;
    private Double totalTaxableAmount;
    private Double totalGST;
    private Double totalCGST;
    private Double totalSGST;
    private Double netRevenue;

    // GST Slab Breakdown
    private List<GstSlabBreakdown> slabBreakdown;

    // Daily GST Breakdown
    private List<DailyGstBreakdown> dailyBreakdown;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class GstSlabBreakdown {
        private Double slabPercentage;
        private Integer transactionCount;
        private Double taxableAmount;
        private Double cgstAmount;
        private Double sgstAmount;
        private Double totalGst;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DailyGstBreakdown {
        private String date;
        private Integer transactionCount;
        private Double taxableAmount;
        private Double gstCollected;
        private Double netRevenue;
    }
}
