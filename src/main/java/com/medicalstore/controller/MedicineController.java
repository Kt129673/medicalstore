package com.medicalstore.controller;

import com.medicalstore.model.Medicine;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.MedicineService;
import com.medicalstore.util.SecurityUtils;
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
    private final BranchService branchService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listMedicines(@RequestParam(required = false) String search, Model model) {
        if (securityUtils.isAdmin()) {
            model.addAttribute("medicines", search != null && !search.isBlank()
                    ? medicineService.searchMedicines(search)
                    : medicineService.getAllMedicines());
        } else if (securityUtils.isOwner()) {
            // owner sees all medicines across their branches
            model.addAttribute("medicines", medicineService.getMedicinesByOwner(securityUtils.getCurrentUserId()));
        } else {
            // shopkeeper: branch-scoped only
            Long branchId = securityUtils.getCurrentBranchId();
            model.addAttribute("medicines", search != null && !search.isBlank()
                    ? medicineService.searchMedicinesByBranch(branchId, search)
                    : medicineService.getMedicinesByBranch(branchId));
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
    public String saveMedicine(@ModelAttribute Medicine medicine,
            RedirectAttributes ra, Model model) {
        try {
            // Set branch for new medicines by shopkeeper
            if (medicine.getId() == null && securityUtils.isShopkeeper()) {
                Long branchId = securityUtils.getCurrentBranchId();
                branchService.getBranchById(branchId).ifPresent(medicine::setBranch);
            }
            medicineService.saveMedicine(medicine);
            ra.addFlashAttribute("success", "Medicine saved successfully!");
            return "redirect:/medicines";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("medicine", medicine);
            return "medicines/form";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to save medicine: " + e.getMessage());
            model.addAttribute("medicine", medicine);
            return "medicines/form";
        }
    }

    @GetMapping("/delete/{id}")
    public String deleteMedicine(@PathVariable Long id, RedirectAttributes ra) {
        medicineService.deleteMedicine(id);
        ra.addFlashAttribute("success", "Medicine deleted successfully!");
        return "redirect:/medicines";
    }

    @GetMapping("/low-stock")
    public String lowStockMedicines(Model model) {
        if (securityUtils.isShopkeeper()) {
            model.addAttribute("medicines",
                    medicineService.getLowStockByBranch(securityUtils.getCurrentBranchId(), 10));
        } else {
            model.addAttribute("medicines", medicineService.getLowStockMedicines(10));
        }
        return "medicines/low-stock";
    }

    @GetMapping("/expiry-alerts")
    public String expiryAlerts(Model model) {
        if (securityUtils.isShopkeeper()) {
            Long bid = securityUtils.getCurrentBranchId();
            model.addAttribute("expiredMedicines", medicineService.getExpiredByBranch(bid));
            model.addAttribute("expiringSoonMedicines", medicineService.getExpiringSoonByBranch(bid, 30));
        } else {
            model.addAttribute("expiredMedicines", medicineService.getExpiredMedicines());
            model.addAttribute("expiringSoonMedicines", medicineService.getExpiringSoonMedicines(30));
        }
        return "medicines/expiry-alerts";
    }
}
