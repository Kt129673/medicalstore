package com.medicalstore.controller.api;

import com.medicalstore.service.DashboardService;
import com.medicalstore.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class DashboardApiController {

    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getDashboardKpis() {
        Map<String, Object> dashboard;

        if (securityUtils.isShopkeeper()) {
            Long branchId = securityUtils.getCurrentBranchId();
            if (branchId == null) {
                // Shopkeeper not yet assigned to a branch — return empty dashboard
                return ResponseEntity.ok(Map.of(
                        "todaySales", 0.0,
                        "monthlyRevenue", 0.0,
                        "totalMedicines", 0,
                        "totalCustomers", 0,
                        "lowStockCount", 0,
                        "criticalStockItems", java.util.List.of(),
                        "recentSales", java.util.List.of()
                ));
            }
            dashboard = dashboardService.buildBranchDashboard(branchId);
        } else if (securityUtils.isOwner()) {
            Long ownerId = securityUtils.getCurrentUserId();
            dashboard = dashboardService.buildOwnerDashboard(ownerId);
        } else {
            dashboard = dashboardService.buildAdminDashboard();
        }

        return ResponseEntity.ok(dashboard);
    }
}
