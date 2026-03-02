package com.medicalstore.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for branch comparison metrics.
 * Optimized to fetch all data in a single query per metric type.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BranchComparisonDTO implements Serializable {
    
    private Long branchId;
    private String branchName;
    private String branchAddress;
    private Boolean isActive;
    
    // Metrics
    private Long totalMedicines;
    private Long totalShopkeepers;
    private Double todaySales;
    private Double monthlyRevenue;
    private Long lowStockCount;
    private Long expiringCount;        // Expiring in 30 days
    private Long activeShopkeepers;    // Active shopkeepers count
    private Long totalCustomers;
    
    // Constructor for JPA projection queries
    public BranchComparisonDTO(Long branchId, String branchName, String branchAddress, Boolean isActive) {
        this.branchId = branchId;
        this.branchName = branchName;
        this.branchAddress = branchAddress;
        this.isActive = isActive;
        this.totalMedicines = 0L;
        this.totalShopkeepers = 0L;
        this.todaySales = 0.0;
        this.monthlyRevenue = 0.0;
        this.lowStockCount = 0L;
        this.expiringCount = 0L;
        this.activeShopkeepers = 0L;
        this.totalCustomers = 0L;
    }
}
