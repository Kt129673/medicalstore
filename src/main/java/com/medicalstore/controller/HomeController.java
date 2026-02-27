package com.medicalstore.controller;

import com.medicalstore.service.DashboardService;
import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Map;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final DashboardService dashboardService;
    private final SecurityUtils securityUtils;

    @GetMapping("/")
    public String home(Model model) {

        // ── OWNER → redirect to owner dashboard ───────────────────────────
        if (securityUtils.isOwner()) {
            return "redirect:/owner";
        }

        // ── Build dashboard data by role ──────────────────────────────────
        Map<String, Object> dashboard;

        if (securityUtils.isShopkeeper()) {
            Long branchId = securityUtils.getCurrentBranchId();
            dashboard = dashboardService.buildBranchDashboard(branchId);
        } else {
            // ADMIN — global
            dashboard = dashboardService.buildAdminDashboard();
        }

        // Add all dashboard data to the model
        dashboard.forEach(model::addAttribute);

        return "index";
    }
}
