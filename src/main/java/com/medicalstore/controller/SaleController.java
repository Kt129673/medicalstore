package com.medicalstore.controller;

import com.medicalstore.model.Sale;
import com.medicalstore.model.Customer;
import com.medicalstore.service.SaleService;
import com.medicalstore.service.CustomerService;
import com.medicalstore.service.WhatsAppService;
import com.medicalstore.service.PdfService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
public class SaleController {

    private final SaleService saleService;
    private final CustomerService customerService;
    private final WhatsAppService whatsAppService;
    private final PdfService pdfService;

    @GetMapping
    public String listSales(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size,
            Model model) {
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size);
        org.springframework.data.domain.Page<Sale> salePage = saleService.getSalesPaginated(pageable);
        model.addAttribute("salePage", salePage);
        return "sales/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("sale", new Sale());
        model.addAttribute("customers", customerService.getAllCustomers());
        return "sales/form";
    }

    @PostMapping("/save")
    @ResponseBody
    public ResponseEntity<?> saveSale(@RequestBody com.medicalstore.dto.SaleRequestDTO saleDto) {
        try {
            Sale sale = new Sale();

            // Map Customer
            if (saleDto.getCustomerId() != null) {
                Customer customer = customerService.getCustomerById(saleDto.getCustomerId()).orElse(null);
                sale.setCustomer(customer);
            }

            sale.setPaymentMethod(saleDto.getPaymentMethod());
            sale.setDiscountPercentage(saleDto.getDiscountPercentage());
            sale.setGstPercentage(saleDto.getGstPercentage());

            // Map Items
            if (saleDto.getItems() != null) {
                for (com.medicalstore.dto.SaleRequestDTO.SaleItemDTO itemDto : saleDto.getItems()) {
                    com.medicalstore.model.SaleItem item = new com.medicalstore.model.SaleItem();

                    com.medicalstore.model.Medicine medicine = new com.medicalstore.model.Medicine();
                    medicine.setId(itemDto.getMedicineId());
                    item.setMedicine(medicine);

                    item.setQuantity(itemDto.getQuantity());
                    item.setUnitPrice(itemDto.getUnitPrice());

                    sale.addItem(item);
                }
            }

            Sale createdSale = saleService.createSale(sale);
            return ResponseEntity.ok(java.util.Map.of(
                    "success", true,
                    "message", "Sale completed successfully!",
                    "saleId", createdSale.getId()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of(
                    "success", false,
                    "message", e.getMessage()));
        }
    }

    /**
     * View Invoice — rendered via Apache Velocity Engine (bypasses Thymeleaf).
     * Returns the fully rendered HTML page directly.
     */
    @GetMapping(value = "/invoice/{id}", produces = MediaType.TEXT_HTML_VALUE)
    @ResponseBody
    public ResponseEntity<String> viewInvoice(@PathVariable Long id) {
        try {
            Sale sale = saleService.getSaleById(id)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));

            String html = pdfService.generateInvoiceHtml(sale);
            return ResponseEntity.ok()
                    .contentType(MediaType.TEXT_HTML)
                    .body(html);
        } catch (Exception e) {
            String errorHtml = "<!DOCTYPE html><html><head><title>Error</title>"
                    + "<link href='https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css' rel='stylesheet'>"
                    + "</head><body class='d-flex justify-content-center align-items-center' style='height:100vh'>"
                    + "<div class='text-center'><h1 class='text-danger'>Invoice Error</h1>"
                    + "<p>" + e.getMessage() + "</p>"
                    + "<a href='/sales' class='btn btn-primary mt-3'>Back to Sales</a></div></body></html>";
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .contentType(MediaType.TEXT_HTML)
                    .body(errorHtml);
        }
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

    @GetMapping("/pdf/{id}")
    public ResponseEntity<byte[]> downloadInvoicePdf(@PathVariable Long id) {
        try {
            Sale sale = saleService.getSaleById(id)
                    .orElseThrow(() -> new RuntimeException("Sale not found"));

            byte[] pdfBytes = pdfService.generateInvoicePdf(sale);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment",
                    String.format("Invoice_INV-%06d.pdf", sale.getId()));
            headers.setCacheControl("must-revalidate, post-check=0, pre-check=0");

            return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
