package com.medicalstore.controller;

import com.medicalstore.model.Customer;
import com.medicalstore.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {
    
    private final CustomerService customerService;
    
    @GetMapping
    public String listCustomers(@RequestParam(required = false) String search, Model model) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("customers", customerService.searchCustomers(search));
        } else {
            model.addAttribute("customers", customerService.getAllCustomers());
        }
        return "customers/list";
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
    public String saveCustomer(@ModelAttribute Customer customer, RedirectAttributes redirectAttributes) {
        customerService.saveCustomer(customer);
        redirectAttributes.addFlashAttribute("success", "Customer saved successfully!");
        return "redirect:/customers";
    }
    
    @GetMapping("/delete/{id}")
    public String deleteCustomer(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            customerService.deleteCustomer(id);
            redirectAttributes.addFlashAttribute("success", "Customer deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Cannot delete customer: it may be linked to sales records.");
        }
        return "redirect:/customers";
    }
}
