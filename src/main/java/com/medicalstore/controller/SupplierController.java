package com.medicalstore.controller;

import com.medicalstore.common.RoutePaths;
import com.medicalstore.model.Supplier;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.SupplierService;
import com.medicalstore.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(RoutePaths.SUPPLIERS)
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
public class SupplierController {
    
    private final SupplierService supplierService;
    private final SecurityUtils securityUtils;
    private final BranchService branchService;
    
    @GetMapping
    public String listSuppliers(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("suppliers", supplierService.searchSuppliers(search));
        } else {
            model.addAttribute("suppliers", supplierService.getAllSuppliers());
        }
        return "suppliers/list";
    }
    
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "suppliers/form";
    }
    
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Supplier supplier = supplierService.getSupplierById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found"));
        model.addAttribute("supplier", supplier);
        return "suppliers/form";
    }
    
    @PostMapping("/save")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public String saveSupplier(@ModelAttribute Supplier supplier, RedirectAttributes redirectAttributes) {
        try {
            // Auto-assign branch for shopkeeper
            if (supplier.getId() == null && securityUtils.isShopkeeper()) {
                Long branchId = securityUtils.getCurrentBranchId();
                if (branchId != null) {
                    branchService.getBranchById(branchId).ifPresent(supplier::setBranch);
                }
            }
            supplierService.saveSupplier(supplier);
            redirectAttributes.addFlashAttribute("success", "Supplier saved successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "A supplier with this phone number or email already exists.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to save supplier: " + e.getMessage());
        }
        return RoutePaths.redirectTo(RoutePaths.SUPPLIERS);
    }
    
    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteSupplier(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            supplierService.deleteSupplier(id);
            redirectAttributes.addFlashAttribute("success", "Supplier deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete supplier: it may be linked to purchase records.");
        }
        return RoutePaths.redirectTo(RoutePaths.SUPPLIERS);
    }
}
