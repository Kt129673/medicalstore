package com.medicalstore.model;

import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "supplier_credits")
public class SupplierCredit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
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
