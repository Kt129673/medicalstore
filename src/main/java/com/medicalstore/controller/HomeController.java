package com.medicalstore.controller;

import com.medicalstore.service.CustomerService;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.SaleService;
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

    @GetMapping("/")
    public String home(Model model) {
        // Use COUNT(*) queries — no full table fetch
        model.addAttribute("totalMedicines", medicineService.countAllMedicines());
        model.addAttribute("lowStock", medicineService.countLowStockMedicines(10));
        model.addAttribute("todaySales", saleService.getTodaySales());
        model.addAttribute("recentSales", saleService.getRecentSales()); // already LIMIT 5
        model.addAttribute("totalCustomers", customerService.countAllCustomers());
        return "index";
    }
}
