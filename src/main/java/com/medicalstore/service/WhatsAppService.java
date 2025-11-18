package com.medicalstore.service;

import com.medicalstore.model.Sale;
import com.medicalstore.model.Customer;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class WhatsAppService {
    
    @Value("${twilio.phone.number}")
    private String twilioPhoneNumber;
    
    @Value("${twilio.whatsapp.enabled:false}")
    private boolean whatsappEnabled;
    
    @Value("${twilio.account.sid}")
    private String accountSid;
    
    public boolean sendInvoice(Sale sale) {
        if (!isWhatsAppEnabled()) {
            log.warn("WhatsApp is disabled. Configure Twilio credentials to enable.");
            return false;
        }
        
        try {
            Customer customer = sale.getCustomer();
            if (customer == null || customer.getPhone() == null) {
                log.error("Customer or phone number is missing");
                return false;
            }
            
            String message = buildInvoiceMessage(sale);
            String customerPhone = formatPhoneNumber(customer.getPhone());
            
            Message.creator(
                new PhoneNumber("whatsapp:" + customerPhone),
                new PhoneNumber("whatsapp:" + twilioPhoneNumber),
                message
            ).create();
            
            log.info("WhatsApp invoice sent to: {}", customerPhone);
            return true;
        } catch (Exception e) {
            log.error("Failed to send WhatsApp message: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean sendExpiryAlert(String medicineName, String expiryDate, String adminPhone) {
        if (!isWhatsAppEnabled()) {
            return false;
        }
        
        try {
            String message = String.format(
                "⚠️ *EXPIRY ALERT*\n\n" +
                "Medicine: *%s*\n" +
                "Expiry Date: %s\n\n" +
                "Please take necessary action.\n\n" +
                "- Medical Store System",
                medicineName, expiryDate
            );
            
            String phone = formatPhoneNumber(adminPhone);
            
            Message.creator(
                new PhoneNumber("whatsapp:" + phone),
                new PhoneNumber("whatsapp:" + twilioPhoneNumber),
                message
            ).create();
            
            log.info("Expiry alert sent to: {}", phone);
            return true;
        } catch (Exception e) {
            log.error("Failed to send expiry alert: {}", e.getMessage());
            return false;
        }
    }
    
    public boolean sendLowStockAlert(String medicineName, int currentStock, String adminPhone) {
        if (!isWhatsAppEnabled()) {
            return false;
        }
        
        try {
            String message = String.format(
                "📉 *LOW STOCK ALERT*\n\n" +
                "Medicine: *%s*\n" +
                "Current Stock: *%d units*\n\n" +
                "Please reorder soon.\n\n" +
                "- Medical Store System",
                medicineName, currentStock
            );
            
            String phone = formatPhoneNumber(adminPhone);
            
            Message.creator(
                new PhoneNumber("whatsapp:" + phone),
                new PhoneNumber("whatsapp:" + twilioPhoneNumber),
                message
            ).create();
            
            log.info("Low stock alert sent to: {}", phone);
            return true;
        } catch (Exception e) {
            log.error("Failed to send low stock alert: {}", e.getMessage());
            return false;
        }
    }
    
    private String buildInvoiceMessage(Sale sale) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm");
        String invoiceNumber = String.format("INV-%06d", sale.getId());
        
        StringBuilder message = new StringBuilder();
        message.append("🏥 *MEDICAL STORE INVOICE*\n\n");
        message.append("📄 Invoice #: ").append(invoiceNumber).append("\n");
        message.append("📅 Date: ").append(sale.getSaleDate().format(formatter)).append("\n\n");
        
        if (sale.getCustomer() != null) {
            message.append("👤 Customer: ").append(sale.getCustomer().getName()).append("\n\n");
        }
        
        message.append("*ITEMS:*\n");
        message.append("━━━━━━━━━━━━━━━━━\n");
        message.append("Medicine: ").append(sale.getMedicine().getName()).append("\n");
        message.append("Quantity: ").append(sale.getQuantity()).append("\n");
        message.append("Unit Price: ₹").append(String.format("%.2f", sale.getUnitPrice())).append("\n");
        message.append("Subtotal: ₹").append(String.format("%.2f", sale.getTotalAmount())).append("\n");
        
        if (sale.getDiscountPercentage() != null && sale.getDiscountPercentage() > 0) {
            message.append("Discount (").append(sale.getDiscountPercentage()).append("%): -₹")
                   .append(String.format("%.2f", sale.getDiscountAmount())).append("\n");
        }
        
        if (sale.getGstPercentage() != null && sale.getGstPercentage() > 0) {
            message.append("GST (").append(sale.getGstPercentage()).append("%): +₹")
                   .append(String.format("%.2f", sale.getGstAmount())).append("\n");
        }
        
        message.append("━━━━━━━━━━━━━━━━━\n");
        Double finalAmount = sale.getFinalAmount() != null ? sale.getFinalAmount() : sale.getTotalAmount();
        message.append("*TOTAL: ₹").append(String.format("%.2f", finalAmount)).append("*\n\n");
        message.append("💳 Payment: ").append(sale.getPaymentMethod()).append("\n\n");
        message.append("Thank you for your business!\n");
        message.append("Get well soon! 💊\n\n");
        message.append("_Medical Store System_");
        
        return message.toString();
    }
    
    private String formatPhoneNumber(String phone) {
        // Remove all non-digit characters
        String cleaned = phone.replaceAll("[^0-9]", "");
        
        // If it's an Indian number without country code, add +91
        if (cleaned.length() == 10) {
            return "+91" + cleaned;
        }
        
        // If it already has country code but no +, add it
        if (!cleaned.startsWith("+")) {
            return "+" + cleaned;
        }
        
        return cleaned;
    }
    
    private boolean isWhatsAppEnabled() {
        return whatsappEnabled && !accountSid.equals("YOUR_ACCOUNT_SID");
    }
    
    public boolean isConfigured() {
        return isWhatsAppEnabled();
    }
}
