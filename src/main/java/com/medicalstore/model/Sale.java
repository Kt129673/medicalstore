package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "sales", indexes = {
        @Index(name = "idx_sale_date",    columnList = "sale_date"),
        @Index(name = "idx_sale_customer",columnList = "customer_id"),
        @Index(name = "idx_sale_branch",  columnList = "branch_id"),
        @Index(name = "idx_sale_payment", columnList = "paymentMethod"),
        @Index(name = "idx_sale_deleted", columnList = "is_deleted")
})
@SQLDelete(sql = "UPDATE sales SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Sale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @org.hibernate.annotations.BatchSize(size = 30)
    private java.util.List<SaleItem> items = new java.util.ArrayList<>();

    // Keep denormalized totals at the header level for fast querying
    @Column(nullable = false)
    private Double totalAmount = 0.0;

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

    /** The customer who made the purchase (Optional) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Branch branch;

    /** Soft-delete flag — set by Hibernate @SQLDelete. Sales are never hard-deleted. */
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    /** Timestamp of soft deletion (null while active). */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
    }

    public void removeItem(SaleItem item) {
        items.remove(item);
        item.setSale(null);
    }

    @PrePersist
    protected void onCreate() {
        saleDate = LocalDateTime.now();

        // Calculate base total from items
        this.totalAmount = items.stream().mapToDouble(i -> i.getQuantity() * i.getUnitPrice()).sum();

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
