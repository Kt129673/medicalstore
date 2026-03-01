package com.medicalstore.controller;

import com.medicalstore.model.Branch;
import com.medicalstore.model.User;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.CustomerService;
import com.medicalstore.service.DashboardService;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.SaleService;
import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
        private final UserRepository userRepository;
        private final SecurityUtils securityUtils;
        private final PasswordEncoder passwordEncoder;
        private final DashboardService dashboardService;

        // â”€â”€â”€ Owner Dashboard â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€â”€
        @GetMapping
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
                        userRepository.countByRole("SHOPKEEPER"));

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

                model.addAttribute("title", branch.getName() + " â€” Detail");
                model.addAttribute("page", "owner");
                model.addAttribute("branch", branch);
                model.addAttribute("shopkeepers", userRepository.findShopkeepersByBranchId(id));

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
                List<Branch> branches = branchService.getBranchesByOwner(ownerId);

                // Build per-branch KPI summary for comparison table
                List<Map<String, Object>> branchStats = new ArrayList<>();
                for (Branch b : branches) {
                        Map<String, Object> stats = new LinkedHashMap<>();
                        Map<String, Object> kpis = dashboardService.buildBranchDashboard(b.getId());
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
                        branchLabels.add(b.getName());
                        todaySalesData.add(((Number) s.get("todaySales")).doubleValue());
                        monthlyData.add(((Number) s.get("monthlyRevenue")).doubleValue());
                        medicinesData.add(((Number) s.get("totalMedicines")).longValue());
                        lowStockData.add(((Number) s.get("lowStockCount")).longValue());
                        totalMedicinesData.add(((Number) s.get("totalMedicines")).longValue());
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
                model.addAttribute("shopkeepers", userRepository.findShopkeepersByOwnerId(ownerId));
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

                // Verify branch belongs to this owner
                Branch branch = branchService.getBranchById(branchId)
                                .filter(b -> b.getOwner().getId().equals(ownerId))
                                .orElseThrow(() -> new RuntimeException("Branch not found or access denied"));

                if (userRepository.existsByUsername(username)) {
                        ra.addFlashAttribute("error", "Username already exists: " + username);
                        return "redirect:/owner/shopkeepers";
                }

                if (email != null && !email.isBlank() && userRepository.existsByEmail(email)) {
                        ra.addFlashAttribute("error", "Email already registered: " + email);
                        return "redirect:/owner/shopkeepers";
                }

                User shopkeeper = new User();
                shopkeeper.setUsername(username);
                shopkeeper.setPassword(passwordEncoder.encode(password));
                shopkeeper.setFullName(fullName);
                shopkeeper.setEmail(email);
                shopkeeper.setEnabled(true);
                shopkeeper.setAccountNonLocked(true);
                shopkeeper.setRoles(Set.of("SHOPKEEPER"));
                shopkeeper.setBranch(branch);
                userRepository.save(shopkeeper);

                ra.addFlashAttribute("success",
                                "Shopkeeper '" + username + "' created and assigned to " + branch.getName());
                return "redirect:/owner/shopkeepers";
        }

        @PostMapping("/shopkeepers/toggle/{id}")
        public String toggleShopkeeper(@PathVariable Long id, RedirectAttributes ra) {
                Long ownerId = securityUtils.getCurrentUserId();
                var myBranchIds = branchService.getBranchesByOwner(ownerId)
                                .stream().map(Branch::getId).collect(java.util.stream.Collectors.toSet());

                userRepository.findById(id).ifPresentOrElse(u -> {
                        if (!u.getRoles().contains("SHOPKEEPER")
                                        || u.getBranch() == null
                                        || !myBranchIds.contains(u.getBranch().getId())) {
                                ra.addFlashAttribute("error", "Access denied: shopkeeper not in your branches.");
                                return;
                        }
                        u.setEnabled(!u.getEnabled());
                        userRepository.save(u);
                        ra.addFlashAttribute("success", "Shopkeeper status updated.");
                }, () -> ra.addFlashAttribute("error", "Shopkeeper not found."));
                return "redirect:/owner/shopkeepers";
        }
}
