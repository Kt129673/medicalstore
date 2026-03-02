package com.medicalstore.controller;

import com.medicalstore.model.Return;
import com.medicalstore.service.ReturnService;
import com.medicalstore.service.SaleService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/returns")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
public class ReturnController {
    
    private final ReturnService returnService;
    private final SaleService saleService;
    
    @GetMapping
    public String listReturns(Model model) {
        model.addAttribute("returns", returnService.getAllReturns());
        return "returns/list";
    }
    
    @GetMapping("/new")
    public String showAddForm(@RequestParam(required = false) Long saleId, Model model) {
        Return returnItem = new Return();
        if (saleId != null) {
            saleService.getSaleById(saleId).ifPresent(returnItem::setSale);
        }
        model.addAttribute("returnItem", returnItem);
        model.addAttribute("sales", saleService.getAllSalesWithItems());
        return "returns/form";
    }
    
    @PostMapping("/save")
    public String saveReturn(@ModelAttribute Return returnItem, RedirectAttributes redirectAttributes) {
        try {
            returnService.createReturn(returnItem);
            redirectAttributes.addFlashAttribute("success", "Return processed successfully and stock updated!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/returns";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteReturn(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            returnService.deleteReturn(id);
            redirectAttributes.addFlashAttribute("success", "Return cancelled and stock adjustment reversed.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete return: " + e.getMessage());
        }
        return "redirect:/returns";
    }
}
