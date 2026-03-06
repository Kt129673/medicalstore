package com.medicalstore.controller;

import com.medicalstore.common.RoutePaths;
import com.medicalstore.model.Customer;
import com.medicalstore.service.BranchService;
import com.medicalstore.service.CustomerService;
import com.medicalstore.service.SaleService;
import com.medicalstore.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping(RoutePaths.CUSTOMERS)
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
public class CustomerController {

    private final CustomerService customerService;
    private final SecurityUtils securityUtils;
    private final BranchService branchService;
    private final SaleService saleService;

    @GetMapping
    public String listCustomers(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("customers", customerService.searchCustomers(search));
        } else {
            model.addAttribute("customers", customerService.getAllCustomers());
        }
        return "customers/list";
    }

    @GetMapping("/{id}")
    public String viewCustomer(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        model.addAttribute("customer", customer);
        model.addAttribute("purchases", saleService.getSalesByCustomer(id));
        return "customers/view";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("customer", new Customer());
        return "customers/form";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model) {
        Customer customer = customerService.getCustomerById(id)
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        model.addAttribute("customer", customer);
        return "customers/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasRole('SHOPKEEPER')")
    public String saveCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        try {
            // Convert empty email to null to avoid UNIQUE constraint violation
            if (customer.getEmail() != null && customer.getEmail().isBlank()) {
                customer.setEmail(null);
            }

            if (customer.getId() != null) {
                // Editing existing customer — preserve branch and registeredDate from DB
                customerService.getCustomerById(customer.getId()).ifPresent(existing -> {
                    customer.setBranch(existing.getBranch());
                    if (customer.getRegisteredDate() == null) {
                        customer.setRegisteredDate(existing.getRegisteredDate());
                    }
                });
            } else {
                // New customer — auto-assign branch for shopkeeper
                if (securityUtils.isShopkeeper()) {
                    Long branchId = securityUtils.getCurrentBranchId();
                    if (branchId != null) {
                        branchService.getBranchById(branchId).ifPresent(customer::setBranch);
                    }
                }
            }

            customerService.saveCustomer(customer);
            redirectAttributes.addFlashAttribute("success", "Customer saved successfully!");
        } catch (org.springframework.dao.DataIntegrityViolationException e) {
            redirectAttributes.addFlashAttribute("error", "A customer with this phone number or email already exists.");
            String returnPath = customer.getId() != null
                    ? RoutePaths.CUSTOMERS + "/edit/" + customer.getId()
                    : RoutePaths.CUSTOMERS + "/new";
            return "redirect:" + returnPath;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to save customer: " + e.getMessage());
            String returnPath = customer.getId() != null
                    ? RoutePaths.CUSTOMERS + "/edit/" + customer.getId()
                    : RoutePaths.CUSTOMERS + "/new";
            return "redirect:" + returnPath;
        }
        return RoutePaths.redirectTo(RoutePaths.CUSTOMERS);
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete customer: it may be linked to sales records.");
        }
        return RoutePaths.redirectTo(RoutePaths.CUSTOMERS);
    }
}
