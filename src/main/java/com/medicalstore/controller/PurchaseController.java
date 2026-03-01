package com.medicalstore.controller;

import com.medicalstore.model.Medicine;
import com.medicalstore.model.PurchaseOrder;
import com.medicalstore.model.PurchaseOrderItem;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.PurchaseService;
import com.medicalstore.service.SupplierService;
import com.medicalstore.util.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/purchases")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final SupplierService supplierService;
    private final MedicineService medicineService;
    private final BranchService branchService;
    private final SecurityUtils securityUtils;

    @GetMapping
    public String listOrders(Model model) {
        model.addAttribute("orders", purchaseService.getAllOrders());
        return "purchase/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("order", new PurchaseOrder());
        populateFormModel(model);
        return "purchase/form";
    }

    @GetMapping("/{id}")
    public String viewOrder(@PathVariable Long id, Model model) {
        PurchaseOrder order = purchaseService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        model.addAttribute("order", order);
        return "purchase/view";
    }

    @GetMapping("/edit/{id}")
    public String editOrder(@PathVariable Long id, Model model) {
        PurchaseOrder order = purchaseService.getOrderById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        if (!"DRAFT".equals(order.getStatus())) {
            return "redirect:/purchases/" + id;
        }
        model.addAttribute("order", order);
        populateFormModel(model);
        return "purchase/form";
    }

    @PostMapping("/save")
    public String saveOrder(@RequestParam Long supplierId,
            @RequestParam String orderDate,
            @RequestParam(required = false) String notes,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long orderId,
            @RequestParam(name = "medicineId", required = false) List<Long> medicineIds,
            @RequestParam(name = "itemQty", required = false) List<Integer> quantities,
            @RequestParam(name = "itemPrice", required = false) List<Double> prices,
            RedirectAttributes ra) {
        try {
            PurchaseOrder order;
            if (orderId != null) {
                order = purchaseService.getOrderById(orderId)
                        .orElseThrow(() -> new RuntimeException("Order not found"));
                order.getItems().clear();
            } else {
                order = new PurchaseOrder();
            }

            supplierService.getSupplierById(supplierId).ifPresent(order::setSupplier);
            order.setOrderDate(java.time.LocalDate.parse(orderDate));
            order.setNotes(notes);
            order.setStatus(status != null ? status : "DRAFT");

            // Set branch
            if (securityUtils.isShopkeeper()) {
                Long branchId = securityUtils.getCurrentBranchId();
                branchService.getBranchById(branchId).ifPresent(order::setBranch);
            }

            // Build items
            if (medicineIds != null) {
                for (int i = 0; i < medicineIds.size(); i++) {
                    PurchaseOrderItem item = new PurchaseOrderItem();
                    Medicine med = medicineService.getMedicineById(medicineIds.get(i))
                            .orElseThrow(() -> new RuntimeException("Medicine not found"));
                    item.setMedicine(med);
                    item.setQuantity(quantities.get(i));
                    item.setUnitPrice(prices.get(i));
                    item.setTotalPrice(quantities.get(i) * prices.get(i));
                    order.getItems().add(item);
                }
            }

            PurchaseOrder saved = purchaseService.saveOrder(order);
            ra.addFlashAttribute("success", "Purchase order " + saved.getOrderNumber() + " saved successfully!");
            return "redirect:/purchases/" + saved.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to save order: " + e.getMessage());
            return "redirect:/purchases/new";
        }
    }

    @PostMapping("/{id}/receive")
    public String receiveOrder(@PathVariable Long id,
            @RequestParam(name = "receivedQty") List<Integer> receivedQuantities,
            RedirectAttributes ra) {
        try {
            purchaseService.receiveOrder(id, receivedQuantities);
            ra.addFlashAttribute("success", "Goods received successfully! Stock has been updated.");
            return "redirect:/purchases/" + id;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Failed to receive goods: " + e.getMessage());
            return "redirect:/purchases/" + id;
        }
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteOrder(@PathVariable Long id, RedirectAttributes ra) {
        try {
            purchaseService.deleteOrder(id);
            ra.addFlashAttribute("success", "Purchase order deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Cannot delete purchase order: " + e.getMessage());
        }
        return "redirect:/purchases";
    }

    @PostMapping("/{id}/cancel")
    public String cancelOrder(@PathVariable Long id, RedirectAttributes ra) {
        purchaseService.cancelOrder(id);
        ra.addFlashAttribute("success", "Purchase order cancelled.");
        return "redirect:/purchases/" + id;
    }

    private void populateFormModel(Model model) {
        model.addAttribute("suppliers", supplierService.getAllSuppliers());
        model.addAttribute("medicines", medicineService.getAllMedicines());
    }
}
