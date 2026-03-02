package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "medicines", indexes = {
        @Index(name = "idx_medicine_name", columnList = "name"),
        @Index(name = "idx_medicine_category", columnList = "category"),
        @Index(name = "idx_medicine_expiry", columnList = "expiry_date"),
        @Index(name = "idx_medicine_quantity", columnList = "quantity"),
        @Index(name = "idx_medicine_branch", columnList = "branch_id"),
        @Index(name = "idx_medicine_qty_expiry", columnList = "quantity, expiry_date"),
        @Index(name = "idx_medicine_barcode", columnList = "barcode"),
        @Index(name = "idx_medicine_batch", columnList = "batch_number")
})
@SQLDelete(sql = "UPDATE medicines SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class Medicine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Optimistic locking version. JPA increments this on every UPDATE.
     * A concurrent write to the same Medicine row will throw
     * OptimisticLockException, preventing lost-update races on stock.
     */
    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String category;

    private String manufacturer;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private Integer quantity;

    private String description;

    @Column(name = "expiry_date")
    @org.springframework.format.annotation.DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate expiryDate;

    @Column(name = "batch_number")
    private String batchNumber;

    @Column(unique = true)
    private String barcode;

    /** HSN/SAC code for GST compliance */
    @Column(name = "hsn_code")
    private String hsnCode;

    /** Active pharmaceutical ingredients */
    @Column(name = "salt_composition")
    private String saltComposition;

    /** Wholesale purchase price (cost) */
    @Column(name = "purchase_price")
    private Double purchasePrice;

    /** Maximum Retail Price */
    private Double mrp;

    /** GST slab: 0, 5, 12, 18, 28 */
    @Column(name = "gst_percentage")
    private Double gstPercentage;

    /** Drug schedule: H, H1, X, OTC, etc. */
    @Column(name = "schedule_type")
    private String scheduleType;

    @Column(name = "created_date")
    private LocalDate createdDate;

    /** Branch this medicine belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Branch branch;

    /** Supplier of this medicine */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Supplier supplier;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
    }
}
