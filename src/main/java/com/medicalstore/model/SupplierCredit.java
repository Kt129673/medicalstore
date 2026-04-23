package com.medicalstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "supplier_credits", indexes = {
        @Index(name = "idx_supplier_credit_branch", columnList = "branch_id"),
        @Index(name = "idx_supplier_credit_supplier", columnList = "supplier_id"),
        @Index(name = "idx_supplier_credit_status", columnList = "status"),
        @Index(name = "idx_supplier_credit_due_date", columnList = "due_date")
})
@SQLDelete(sql = "UPDATE supplier_credits SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class SupplierCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    @JsonIgnore
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @JsonIgnore
    private Branch branch;

    @Column(nullable = false)
    private Double totalDue = 0.0;

    @Column(nullable = false)
    private Double paidAmount = 0.0;

    @Column(nullable = false)
    private LocalDate dueDate;

    private String status = "PENDING"; // PENDING, PARTIAL, PAID, OVERDUE

    @Column(nullable = false, updatable = false)
    private LocalDate createdDate = LocalDate.now();

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    public Double getRemainingAmount() {
        return totalDue - paidAmount;
    }

    public void updateStatus() {
        if (paidAmount >= totalDue) {
            this.status = "PAID";
        } else if (paidAmount > 0) {
            this.status = "PARTIAL";
        } else if (dueDate != null && LocalDate.now().isAfter(dueDate)) {
            this.status = "OVERDUE";
        } else {
            this.status = "PENDING";
        }
    }
}
