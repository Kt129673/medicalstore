package com.medicalstore.controller.api;

import com.medicalstore.service.DashboardService;
import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardApiController {

    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    @GetMapping("/kpis")
    public ResponseEntity<Map<String, Object>> getDashboardKpis() {
        Map<String, Object> dashboard;

        if (securityUtils.isShopkeeper()) {
            Long branchId = securityUtils.getCurrentBranchId();
            dashboard = dashboardService.buildBranchDashboard(branchId);
        } else {
            dashboard = dashboardService.buildAdminDashboard();
        }

        return ResponseEntity.ok(dashboard);
    }
}
