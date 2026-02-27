package com.medicalstore.controller;

import com.medicalstore.model.SupplierCredit;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.SupplierCreditService;
import com.medicalstore.service.SupplierService;
import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suppliers/credits")
@RequiredArgsConstructor
public class SupplierCreditController {

    private final SupplierCreditService creditService;
    private final SupplierService supplierService;
    private final BranchService branchService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listCredits(Model model) {
        model.addAttribute("credits", creditService.getAllCredits());
        model.addAttribute("agingReport", creditService.getAgingReport());
        model.addAttribute("overdueCount", creditService.getOverdueCredits().size());
        return "suppliers/credits/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("credit", new SupplierCredit());
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        return "suppliers/credits/form";
    }

    @PostMapping("/save")
    public String saveCredit(@ModelAttribute SupplierCredit credit, RedirectAttributes ra) {
        try {
            // Auto-assign branch for shopkeepers
            if (credit.getId() == null && securityUtils.isShopkeeper()) {
                branchService.getBranchById(securityUtils.getCurrentBranchId()).ifPresent(credit::setBranch);
            }
            creditService.saveCredit(credit);
            ra.addFlashAttribute("success", "Credit record saved successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Error saving credit: " + e.getMessage());
        }
        return "redirect:/suppliers/credits";
    }

    @PostMapping("/{id}/pay")
    public String recordPayment(@PathVariable Long id, @RequestParam Double amount, RedirectAttributes ra) {
        try {
            creditService.recordPayment(id, amount);
            ra.addFlashAttribute("success", "Payment of ₹" + amount + " recorded successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("error", "An error occurred while recording payment.");
        }
        return "redirect:/suppliers/credits";
    }

    @GetMapping("/overdue")
    public String listOverdue(Model model) {
        model.addAttribute("credits", creditService.getOverdueCredits());
        model.addAttribute("isOverdueView", true);
        model.addAttribute("agingReport", creditService.getAgingReport());
        return "suppliers/credits/list";
    }
}
