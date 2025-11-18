package com.medicalstore.controller;

import com.medicalstore.model.Sale;
import com.medicalstore.model.Customer;
import com.medicalstore.service.SaleService;
import com.medicalstore.service.MedicineService;
import com.medicalstore.service.CustomerService;
import com.medicalstore.service.WhatsAppService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {
    
    private final SaleService saleService;
    private final MedicineService medicineService;
    private final CustomerService customerService;
    private final WhatsAppService whatsAppService;
    
    @GetMapping
    public String listSales(Model model) {
        model.addAttribute("sales", saleService.getAllSales());
        return "sales/list";
    }
    
    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("sale", new Sale());
        model.addAttribute("medicines", medicineService.getAllMedicines());
        model.addAttribute("customers", customerService.getAllCustomers());
        return "sales/form";
    }
    
    @PostMapping("/save")
    public String saveSale(@ModelAttribute Sale sale, RedirectAttributes redirectAttributes) {
        try {
            // Handle optional customer - if customer ID is null/0, set customer to null
            if (sale.getCustomer() != null && sale.getCustomer().getId() == null) {
                sale.setCustomer(null);
            } else if (sale.getCustomer() != null && sale.getCustomer().getId() != null) {
                // Fetch full customer object
                Customer customer = customerService.getCustomerById(sale.getCustomer().getId())
                        .orElse(null);
                sale.setCustomer(customer);
            }
            
            saleService.createSale(sale);
            redirectAttributes.addFlashAttribute("success", "Sale completed successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sales";
    }
    
    @GetMapping("/invoice/{id}")
    public String viewInvoice(@PathVariable Long id, Model model) {
        Sale sale = saleService.getSaleById(id)
                .orElseThrow(() -> new RuntimeException("Sale not found"));
        model.addAttribute("sale", sale);
        model.addAttribute("whatsappEnabled", whatsAppService.isConfigured());
        return "sales/invoice";
    }
    
    @PostMapping("/send-whatsapp/{id}")
    @ResponseBody
    public String sendWhatsApp(@PathVariable Long id) {
        try {
            Sale sale = saleService.getSaleById(id)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));
            
            if (!whatsAppService.isConfigured()) {
                return "WhatsApp is not configured. Please set up Twilio credentials.";
            }
            
            if (sale.getCustomer() == null || sale.getCustomer().getPhone() == null) {
                return "Customer phone number is required to send WhatsApp message.";
            }
            
            boolean sent = whatsAppService.sendInvoice(sale);
            if (sent) {
                return "Invoice sent successfully via WhatsApp!";
            } else {
                return "Failed to send WhatsApp message. Please check logs.";
            }
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}
