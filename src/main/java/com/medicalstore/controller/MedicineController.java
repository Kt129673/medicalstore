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
    public String listMedicines(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String stockLevel,
            @RequestParam(required = false) String expiryRange,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {

        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by("name").ascending());
        org.springframework.data.domain.Page<Medicine> medicinePage = medicineService.filterMedicines(search, category,
                stockLevel, expiryRange, pageable);

        model.addAttribute("medicinePage", medicinePage);
        model.addAttribute("categories", medicineService.getAllCategories());

        // Preserve filter states
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentCategory", category);
        model.addAttribute("currentStockLevel", stockLevel);
        model.addAttribute("currentExpiryRange", expiryRange);
        model.addAttribute("currentPageSize", size);

        return "medicines/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("medicine", new Medicine());
        model.addAttribute("categories", medicineService.getAllCategories());
        return "medicines/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Medicine medicine = medicineService.getMedicineById(id)
                .orElseThrow(() -> new RuntimeException("Medicine not found"));
        model.addAttribute("medicine", medicine);
        model.addAttribute("categories", medicineService.getAllCategories());
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

    @PostMapping("/bulk-delete")
    public String bulkDeleteMedicines(@RequestParam("ids") java.util.List<Long> ids, RedirectAttributes ra) {
        int count = 0;
        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                medicineService.deleteMedicine(id);
                count++;
            }
        }
        ra.addFlashAttribute("success", count + " medicines deleted successfully!");
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
