package com.medicalstore.controller;

import com.medicalstore.model.Medicine;
import com.medicalstore.service.MedicineService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/medicines")
@RequiredArgsConstructor
public class MedicineController {
    
    private final MedicineService medicineService;
    
    @GetMapping
    public String listMedicines(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("medicines", medicineService.searchMedicines(search));
        } else {
            model.addAttribute("medicines", medicineService.getAllMedicines());
        }
        return "medicines/list";
    }
    
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("medicine", new Medicine());
        return "medicines/form";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Medicine medicine = medicineService.getMedicineById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
        model.addAttribute("medicine", medicine);
        return "medicines/form";
    }
    
    @PostMapping("/save")
    public String saveMedicine(@ModelAttribute Medicine medicine, RedirectAttributes redirectAttributes) {
        medicineService.saveMedicine(medicine);
        redirectAttributes.addFlashAttribute("success", "Medicine saved successfully!");
        return "redirect:/medicines";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteMedicine(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        medicineService.deleteMedicine(id);
        redirectAttributes.addFlashAttribute("success", "Medicine deleted successfully!");
        return "redirect:/medicines";
    }
    
    @GetMapping("/low-stock")
    public String lowStockMedicines(Model model) {
        model.addAttribute("medicines", medicineService.getLowStockMedicines(10));
        return "medicines/low-stock";
    }
    
    @GetMapping("/expiry-alerts")
    public String expiryAlerts(Model model) {
        model.addAttribute("expiredMedicines", medicineService.getExpiredMedicines());
        model.addAttribute("expiringSoonMedicines", medicineService.getExpiringSoonMedicines(30));
        return "medicines/expiry-alerts";
    }
}
