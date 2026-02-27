package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sale_date", columnList = "sale_date"),
        @Index(name = "idx_sale_medicine", columnList = "medicine_id"),
        @Index(name = "idx_sale_customer", columnList = "customer_id"),
        @Index(name = "idx_sale_branch", columnList = "branch_id"),
        @Index(name = "idx_sale_payment", columnList = "paymentMethod"),
        @Index(name = "idx_sale_date_medicine", columnList = "sale_date, medicine_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @ManyToOne(fetch = FetchType.LAZY)
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

    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate;

    private String paymentMethod; // Cash, Card, UPI

    /** Branch this sale belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

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
