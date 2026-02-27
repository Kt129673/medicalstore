package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders", indexes = {
        @Index(name = "idx_po_branch", columnList = "branch_id"),
        @Index(name = "idx_po_supplier", columnList = "supplier_id"),
        @Index(name = "idx_po_status", columnList = "status"),
        @Index(name = "idx_po_date", columnList = "order_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    @Column(name = "order_date", nullable = false)
    private LocalDate orderDate;

    @Column(nullable = false)
    private String status; // DRAFT, ORDERED, RECEIVED, CANCELLED

    @OneToMany(mappedBy = "purchaseOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseOrderItem> items = new ArrayList<>();

    @Column(name = "total_amount")
    private Double totalAmount = 0.0;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "received_date")
    private LocalDate receivedDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (orderDate == null)
            orderDate = LocalDate.now();
        if (status == null)
            status = "DRAFT";
    }

    /** Helper: recalculate total from items */
    public void recalculateTotal() {
        this.totalAmount = items.stream()
                .mapToDouble(i -> i.getTotalPrice() != null ? i.getTotalPrice() : 0.0)
                .sum();
    }
}
