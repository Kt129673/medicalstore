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
        model.addAttribute("totalMedicines", medicineService.getAllMedicines().size());
        model.addAttribute("lowStock", medicineService.getLowStockMedicines(10).size());
        model.addAttribute("todaySales", saleService.getTodaySales());
        model.addAttribute("recentSales", saleService.getRecentSales().stream().limit(5).toList());
        model.addAttribute("totalCustomers", customerService.getAllCustomers().size());
        return "index";
    }
}
