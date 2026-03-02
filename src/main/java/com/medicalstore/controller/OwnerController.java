package com.medicalstore.controller;

import com.medicalstore.model.Branch;
import com.medicalstore.model.SubscriptionPlan;
import com.medicalstore.model.User;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.CustomerService;
import com.medicalstore.service.DashboardService;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.SaleService;
import com.medicalstore.service.SubscriptionService;
import com.medicalstore.service.UserManagementService;
import com.medicalstore.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;

/**
 * Owner panel â€” view all owned branches, combined stats,
 * and manage shopkeepers for their branches.
 * Access restricted to ROLE_OWNER only (via SecurityConfig).
 */
@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
@PreAuthorize("hasRole('OWNER')")
public class OwnerController {

        private final BranchService branchService;
        private final MedicineService medicineService;
        private final SaleService saleService;
        private final CustomerService customerService;
        private final UserManagementService userManagementService;
        private final SecurityUtils securityUtils;
        private final DashboardService dashboardService;
        private final SubscriptionService subscriptionService;

        // â”€â”€â”€ Owner Dashboard â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        @GetMapping({"", "/dashboard"})
        public String dashboard(Model model) {
                Long ownerId = securityUtils.getCurrentUserId();
                List<Branch> branches = branchService.getBranchesByOwner(ownerId);

                // Use DashboardService for fully aggregated, properly scoped KPIs
                Map<String, Object> kpis = dashboardService.buildOwnerDashboard(ownerId);

                model.addAttribute("title", "Owner Dashboard");
                model.addAttribute("page", "owner");
                model.addAttribute("branches", branches);

                // Populate all KPI attributes from dashboard service
                kpis.forEach(model::addAttribute);

                // Alias for template backward-compat
                model.addAttribute("lowStock", kpis.get("lowStockCount"));
                model.addAttribute("branchCount", branches.size());
                model.addAttribute("shopkeeperCount",
                        userManagementService.countByRole("SHOPKEEPER"));

                return "owner/dashboard";
        }

        // â”€â”€â”€ Branch Details â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        @GetMapping("/branches/{id}")
        public String branchDetail(@PathVariable Long id, Model model) {
                Long ownerId = securityUtils.getCurrentUserId();
                Branch branch = branchService.getBranchById(id)
                                .filter(b -> b.getOwner().getId().equals(ownerId))
                                .orElseThrow(() -> new RuntimeException("Branch not found or access denied"));

                // Build branch-scoped KPI data
                Map<String, Object> kpis = dashboardService.buildBranchDashboard(id);

                model.addAttribute("title", branch.getName() + " \u2013 Detail");
                model.addAttribute("page", "owner");
                model.addAttribute("branch", branch);
                model.addAttribute("shopkeepers", userManagementService.findShopkeepersByBranchId(id));

                // Medicines and recent sales from KPI data
                model.addAttribute("medicines", medicineService.getMedicinesByBranch(id));

                // Provide all KPI attributes for the template
                kpis.forEach(model::addAttribute);
                model.addAttribute("lowStock", kpis.get("lowStockCount"));

                return "owner/branch-detail";
        }

        // â”€â”€â”€ Branch Comparison â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        @GetMapping("/compare")
        public String compareBranches(Model model) {
                Long ownerId = securityUtils.getCurrentUserId();
                if (ownerId == null) {
                        return "redirect:/login";
                }

                List<Branch> branches = branchService.getBranchesByOwner(ownerId);
                if (branches == null) {
                        branches = new ArrayList<>();
                }

                // Build per-branch KPI summary for comparison table
                List<Map<String, Object>> branchStats = new ArrayList<>();
                for (Branch b : branches) {
                        Map<String, Object> stats = new LinkedHashMap<>();
                        Map<String, Object> kpis = dashboardService.buildBranchDashboard(b.getId());
                        if (kpis == null) {
                                kpis = new LinkedHashMap<>();
                        }
                        stats.put("branch", b);
                        stats.put("todaySales", kpis.getOrDefault("todaySales", 0.0));
                        stats.put("monthlyRevenue", kpis.getOrDefault("monthlyRevenue", 0.0));
                        stats.put("totalMedicines", kpis.getOrDefault("totalMedicines", 0L));
                        stats.put("totalCustomers", kpis.getOrDefault("totalCustomers", 0L));
                        stats.put("lowStockCount", kpis.getOrDefault("lowStockCount", 0L));
                        stats.put("expiringIn30", kpis.getOrDefault("expiringIn30", 0L));
                        branchStats.add(stats);
                }

                // Build flat arrays for Chart.js
                List<String> branchLabels    = new ArrayList<>();
                List<Double> todaySalesData  = new ArrayList<>();
                List<Double> monthlyData     = new ArrayList<>();
                List<Long>   medicinesData   = new ArrayList<>();
                List<Long>   lowStockData    = new ArrayList<>();
                List<Long>   totalMedicinesData = new ArrayList<>();
                for (Map<String, Object> s : branchStats) {
                        Branch b = (Branch) s.get("branch");
                        if (b != null) {
                                branchLabels.add(b.getName());
                                Object todaySalesObj = s.get("todaySales");
                                Object monthlyObj = s.get("monthlyRevenue");
                                Object medicinesObj = s.get("totalMedicines");
                                Object lowStockObj = s.get("lowStockCount");
                                
                                todaySalesData.add(todaySalesObj != null ? ((Number) todaySalesObj).doubleValue() : 0.0);
                                monthlyData.add(monthlyObj != null ? ((Number) monthlyObj).doubleValue() : 0.0);
                                medicinesData.add(medicinesObj != null ? ((Number) medicinesObj).longValue() : 0L);
                                lowStockData.add(lowStockObj != null ? ((Number) lowStockObj).longValue() : 0L);
                                totalMedicinesData.add(medicinesObj != null ? ((Number) medicinesObj).longValue() : 0L);
                        }
                }

                model.addAttribute("title", "Branch Comparison");
                model.addAttribute("page", "owner");
                model.addAttribute("branches", branches);
                model.addAttribute("branchStats", branchStats);
                model.addAttribute("branchLabels", branchLabels);
                model.addAttribute("todaySalesData", todaySalesData);
                model.addAttribute("monthlyData", monthlyData);
                model.addAttribute("medicinesData", medicinesData);
                model.addAttribute("lowStockData", lowStockData);
                model.addAttribute("totalMedicinesData", totalMedicinesData);
                return "owner/compare";
        }

        // â”€â”€â”€ Shopkeeper Management â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        @GetMapping("/shopkeepers")
        public String listShopkeepers(Model model) {
                Long ownerId = securityUtils.getCurrentUserId();

                model.addAttribute("title", "My Shopkeepers");
                model.addAttribute("page", "owner");
                model.addAttribute("shopkeepers", userManagementService.findShopkeepersByOwnerId(ownerId));
                model.addAttribute("branches", branchService.getBranchesByOwner(ownerId));
                return "owner/shopkeepers";
        }

        @PostMapping("/shopkeepers/create")
        public String createShopkeeper(
                        @RequestParam String username,
                        @RequestParam String password,
                        @RequestParam String fullName,
                        @RequestParam String email,
                        @RequestParam Long branchId,
                        RedirectAttributes ra) {

                Long ownerId = securityUtils.getCurrentUserId();

                // Verify branch belongs to this owner (tenant validation in controller is
                // acceptable here since it's about ownership of the branch resource)
                Branch branch = branchService.getBranchById(branchId)
                                .filter(b -> b.getOwner().getId().equals(ownerId))
                                .orElseThrow(() -> new RuntimeException("Branch not found or access denied"));

                // All uniqueness checks + encoding + save handled by UserManagementService
                userManagementService.createUser(username, password, fullName, email, "SHOPKEEPER", branch);

                ra.addFlashAttribute("success",
                                "Shopkeeper '" + username + "' created and assigned to " + branch.getName());
                return "redirect:/owner/shopkeepers";
        }

        @PostMapping("/shopkeepers/toggle/{id}")
        public String toggleShopkeeper(@PathVariable Long id, RedirectAttributes ra) {
                Long ownerId = securityUtils.getCurrentUserId();
                var myBranchIds = branchService.getBranchesByOwner(ownerId)
                                .stream().map(Branch::getId).collect(java.util.stream.Collectors.toSet());
                userManagementService.toggleShopkeeper(id, myBranchIds);
                ra.addFlashAttribute("success", "Shopkeeper status updated.");
                return "redirect:/owner/shopkeepers";
        }

        // ─── Subscription Status ───────────────────────────────────────────────
        @GetMapping("/subscription")
        public String subscriptionStatus(Model model) {
                Long ownerId = securityUtils.getCurrentUserId();
                SubscriptionPlan plan = subscriptionService.getPlanForOwner(ownerId).orElse(null);

                model.addAttribute("title", "Subscription Status");
                model.addAttribute("page", "owner");
                model.addAttribute("plan", plan);

                if (plan != null && !plan.isExpired()) {
                        model.addAttribute("daysLeft", plan.getDaysUntilExpiry());
                }

                return "owner/subscription";
        }

        // ─── Owner → Shopkeeper View Mode (Section 4.2) ────────────────────────
        /**
         * Enter Shopkeeper View mode: owner sees a read-only operational summary
         * for a specific shopkeeper, with a banner —
         * "Viewing as Shopkeeper: [Name] (Owner Mode)".
         *
         * Flow:
         *   1. Owner clicks a shopkeeper's profile in /owner/shopkeepers.
         *   2. System renders a shopkeeper-context summary page.
         *   3. A top banner identifies the view context.
         *   4. Owner can exit back to /owner/shopkeepers.
         *
         * The owner cannot directly use the shopkeeper's POS; this is a view-only
         * context for monitoring operational status.
         */
        @GetMapping("/view-as-shopkeeper/{shopkeeperId}")
        public String viewAsShopkeeper(@PathVariable Long shopkeeperId, Model model, RedirectAttributes ra) {
                Long ownerId = securityUtils.getCurrentUserId();
                var myBranchIds = branchService.getBranchesByOwner(ownerId)
                                .stream().map(Branch::getId).collect(java.util.stream.Collectors.toSet());

                User shopkeeper = userManagementService.findById(shopkeeperId).orElse(null);
                if (shopkeeper == null
                                || !shopkeeper.getRoles().contains("SHOPKEEPER")
                                || shopkeeper.getBranch() == null
                                || !myBranchIds.contains(shopkeeper.getBranch().getId())) {
                        ra.addFlashAttribute("error", "Shopkeeper not found or not in your branches.");
                        return "redirect:/owner/shopkeepers";
                }

                Branch branch = shopkeeper.getBranch();
                Map<String, Object> kpis = dashboardService.buildBranchDashboard(branch.getId());

                model.addAttribute("title", "Viewing as Shopkeeper: " + shopkeeper.getFullName());
                model.addAttribute("page", "owner");
                model.addAttribute("branch", branch);
                model.addAttribute("medicines", medicineService.getMedicinesByBranch(branch.getId()));
                model.addAttribute("shopkeepers", userManagementService.findShopkeepersByBranchId(branch.getId()));
                kpis.forEach(model::addAttribute);
                model.addAttribute("lowStock", kpis.get("lowStockCount"));

                // Shopkeeper view context — banner in owner/branch-detail.html
                model.addAttribute("shopkeeperViewMode", true);
                model.addAttribute("viewingShopkeeperName", shopkeeper.getFullName());
                model.addAttribute("exitShopkeeperViewUrl", "/owner/shopkeepers");

                return "owner/branch-detail";
        }
}
