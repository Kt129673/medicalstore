package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "purchase_order_items", indexes = {
        @Index(name = "idx_poi_order", columnList = "purchase_order_id"),
        @Index(name = "idx_poi_medicine", columnList = "medicine_id")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PurchaseOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicine_id", nullable = false)
    private Medicine medicine;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private Double unitPrice;

    @Column(name = "total_price")
    private Double totalPrice;

    @Column(name = "received_quantity")
    private Integer receivedQuantity = 0;

    @PrePersist
    @PreUpdate
    protected void calculateTotal() {
        if (quantity != null && unitPrice != null) {
            totalPrice = quantity * unitPrice;
        }
    }
}
