package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;
    
    @ManyToOne
    @JoinColumn(name = "customer_id")
    private Customer customer;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private Double unitPrice;
    
    @Column(nullable = false)
    private Double totalAmount;
    
    @Column(name = "discount_percentage")
    private Double discountPercentage = 0.0;
    
    @Column(name = "discount_amount")
    private Double discountAmount = 0.0;
    
    @Column(name = "gst_percentage")
    private Double gstPercentage = 0.0;
    
    @Column(name = "gst_amount")
    private Double gstAmount = 0.0;
    
    @Column(name = "final_amount")
    private Double finalAmount;
    
    @Column(name = "sale_date")
    private LocalDateTime saleDate;
    
    private String paymentMethod; // Cash, Card, UPI
    
    @PrePersist
    protected void onCreate() {
        saleDate = LocalDateTime.now();
        
        // Calculate base total
        totalAmount = quantity * unitPrice;
        
        // Calculate discount
        if (discountPercentage != null && discountPercentage > 0) {
            discountAmount = (totalAmount * discountPercentage) / 100;
        } else {
            discountAmount = 0.0;
        }
        
        // Amount after discount
        Double amountAfterDiscount = totalAmount - discountAmount;
        
        // Calculate GST
        if (gstPercentage != null && gstPercentage > 0) {
            gstAmount = (amountAfterDiscount * gstPercentage) / 100;
        } else {
            gstAmount = 0.0;
        }
        
        // Final amount
        finalAmount = amountAfterDiscount + gstAmount;
    }
}
