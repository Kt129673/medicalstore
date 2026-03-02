package com.medicalstore.controller;

import com.medicalstore.common.RoutePaths;
import com.medicalstore.model.Medicine;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.SupplierService;
import com.medicalstore.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(RoutePaths.MEDICINES)
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
public class MedicineController {

    private final MedicineService medicineService;
    private final BranchService branchService;
    private final SupplierService supplierService;
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
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        if (securityUtils.isAdmin()) {
            model.addAttribute("branches", branchService.getAllBranches());
        }
        return "medicines/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Medicine medicine = medicineService.getMedicineById(id)
                .orElseThrow(() -> new org.springframework.security.access.AccessDeniedException(
                        "Medicine not found or access denied"));
        model.addAttribute("medicine", medicine);
        model.addAttribute("categories", medicineService.getAllCategories());
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        if (securityUtils.isAdmin()) {
            model.addAttribute("branches", branchService.getAllBranches());
        }
        return "medicines/form";
    }

    @PostMapping("/save")
    public String saveMedicine(@ModelAttribute Medicine medicine,
            @RequestParam(required = false) Long branchId,
            RedirectAttributes ra, Model model) {
        try {
            if (medicine.getId() == null) {
                // New medicine: auto-assign branch from context
                if (securityUtils.isShopkeeper()) {
                    Long bid = securityUtils.getCurrentBranchId();
                    branchService.getBranchById(bid).ifPresent(medicine::setBranch);
                } else if (securityUtils.isAdmin() && branchId != null) {
                    branchService.getBranchById(branchId).ifPresent(medicine::setBranch);
                }
            }
            medicineService.saveMedicine(medicine);
            ra.addFlashAttribute("success", "Medicine saved successfully!");
            return RoutePaths.redirectTo(RoutePaths.MEDICINES);
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("medicine", medicine);
            if (securityUtils.isAdmin()) {
                model.addAttribute("branches", branchService.getAllBranches());
            }
            model.addAttribute("categories", medicineService.getAllCategories());
            model.addAttribute("suppliers", supplierService.getAllSuppliers());
            return "medicines/form";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to save medicine: " + e.getMessage());
            model.addAttribute("medicine", medicine);
            if (securityUtils.isAdmin()) {
                model.addAttribute("branches", branchService.getAllBranches());
            }
            model.addAttribute("categories", medicineService.getAllCategories());
            model.addAttribute("suppliers", supplierService.getAllSuppliers());
            return "medicines/form";
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasPermission(null, 'MEDICINE_DELETE')")
    public String deleteMedicine(@PathVariable Long id, RedirectAttributes ra) {
        try {
            medicineService.deleteMedicine(id);
            ra.addFlashAttribute("success", "Medicine deleted successfully!");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Cannot delete medicine: it may be linked to sales or purchases.");
        }
        return RoutePaths.redirectTo(RoutePaths.MEDICINES);
    }

    @PostMapping("/bulk-delete")
    @PreAuthorize("hasPermission(null, 'MEDICINE_BULK_IMPORT')")
    public String bulkDeleteMedicines(@RequestParam("ids") java.util.List<Long> ids, RedirectAttributes ra) {
        int count = 0;
        if (ids != null && !ids.isEmpty()) {
            for (Long id : ids) {
                medicineService.deleteMedicine(id);
                count++;
            }
        }
        ra.addFlashAttribute("success", count + " medicines deleted successfully!");
        return RoutePaths.redirectTo(RoutePaths.MEDICINES);
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
