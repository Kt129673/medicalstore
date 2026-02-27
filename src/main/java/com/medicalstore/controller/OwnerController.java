package com.medicalstore.controller;

import com.medicalstore.model.Branch;
import com.medicalstore.model.User;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.CustomerService;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.SaleService;
import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

/**
 * Owner panel — view all owned branches, combined stats,
 * and manage shopkeepers for their branches.
 * Access restricted to ROLE_OWNER only (via SecurityConfig).
 */
@Controller
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerController {

        private final BranchService branchService;
        private final MedicineService medicineService;
        private final SaleService saleService;
        private final CustomerService customerService;
        private final UserRepository userRepository;
        private final SecurityUtils securityUtils;
        private final PasswordEncoder passwordEncoder;

        // ─── Owner Dashboard ───────────────────────────────────────────────────
        @GetMapping
        public String dashboard(Model model) {
                Long ownerId = securityUtils.getCurrentUserId();

                model.addAttribute("title", "Owner Dashboard");
                model.addAttribute("page", "owner");
                model.addAttribute("branches", branchService.getBranchesByOwner(ownerId));

                // Aggregated stats across all owner's branches
                model.addAttribute("totalMedicines", medicineService.countAllMedicines());
                model.addAttribute("totalCustomers", customerService.countAllCustomers());
                model.addAttribute("todaySales", saleService.getTodaySales());
                model.addAttribute("lowStock", medicineService.countLowStockMedicines(10));
                model.addAttribute("recentSales", saleService.getRecentSales());

                return "owner/dashboard";
        }

        // ─── Branch Details ────────────────────────────────────────────────────
        @GetMapping("/branches/{id}")
        public String branchDetail(@PathVariable Long id, Model model) {
                Long ownerId = securityUtils.getCurrentUserId();
                Branch branch = branchService.getBranchById(id)
                                .filter(b -> b.getOwner().getId().equals(ownerId))
                                .orElseThrow(() -> new RuntimeException("Branch not found or access denied"));

                // Temporarily override Tenant Context for this request to scope data to the
                // specific branch
                com.medicalstore.config.TenantContext.setTenantId(id);
                com.medicalstore.config.TenantContext.clearOwner();

                model.addAttribute("title", branch.getName() + " Detail");
                model.addAttribute("page", "owner");
                model.addAttribute("branch", branch);
                model.addAttribute("medicines", medicineService.getAllMedicines());
                model.addAttribute("recentSales", saleService.getRecentSales());
                model.addAttribute("shopkeepers", userRepository.findAll().stream()
                                .filter(u -> u.getRoles().contains("SHOPKEEPER")
                                                && u.getBranch() != null
                                                && u.getBranch().getId().equals(id))
                                .toList());

                // Restore context state is handled by the TenantFilter `finally` block
                return "owner/branch-detail";
        }

        // ─── Shopkeeper Management ─────────────────────────────────────────────
        @GetMapping("/shopkeepers")
        public String listShopkeepers(Model model) {
                Long ownerId = securityUtils.getCurrentUserId();
                var myBranchIds = branchService.getBranchesByOwner(ownerId)
                                .stream().map(Branch::getId).toList();

                model.addAttribute("title", "My Shopkeepers");
                model.addAttribute("page", "owner");
                model.addAttribute("shopkeepers", userRepository.findAll().stream()
                                .filter(u -> u.getRoles().contains("SHOPKEEPER")
                                                && u.getBranch() != null
                                                && myBranchIds.contains(u.getBranch().getId()))
                                .toList());
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

        @GetMapping("/shopkeepers/toggle/{id}")
        public String toggleShopkeeper(@PathVariable Long id, RedirectAttributes ra) {
                userRepository.findById(id).ifPresent(u -> {
                        u.setEnabled(!u.getEnabled());
                        userRepository.save(u);
                });
                ra.addFlashAttribute("success", "Shopkeeper status updated.");
                return "redirect:/owner/shopkeepers";
        }
}
