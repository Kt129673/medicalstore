package com.medicalstore.controller;

import com.medicalstore.service.CustomerService;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.SaleService;
import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MedicineService medicineService;
    private final SaleService saleService;
    private final CustomerService customerService;
    private final SecurityUtils securityUtils;

    @GetMapping("/")
    public String home(Model model) {

        // ── OWNER → redirect to owner dashboard ───────────────────────────
        if (securityUtils.isOwner()) {
            return "redirect:/owner";
        }

        // ── SHOPKEEPER or ADMIN ───────────────────────────────────────────
        if (securityUtils.isShopkeeper()) {
            Long branchId = securityUtils.getCurrentBranchId();
            model.addAttribute("totalMedicines", medicineService.countByBranch(branchId));
            model.addAttribute("lowStock", medicineService.countLowStockByBranch(branchId, 10));
            model.addAttribute("todaySales", saleService.getTodaySalesByBranch(branchId));
            model.addAttribute("recentSales", saleService.getRecentSalesByBranch(branchId));
            model.addAttribute("totalCustomers", customerService.countByBranch(branchId));
        } else {
            // ADMIN — global counts
            model.addAttribute("totalMedicines", medicineService.countAllMedicines());
            model.addAttribute("lowStock", medicineService.countLowStockMedicines(10));
            model.addAttribute("todaySales", saleService.getTodaySales());
            model.addAttribute("recentSales", saleService.getRecentSales());
            model.addAttribute("totalCustomers", customerService.countAllCustomers());
        }

        return "index";
    }
}
