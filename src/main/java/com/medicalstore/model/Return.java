package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "returns", indexes = {
        @Index(name = "idx_return_sale", columnList = "sale_id"),
        @Index(name = "idx_return_date", columnList = "return_date")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(nullable = false)
    private Integer returnQuantity;

    @Column(nullable = false)
    private Double refundAmount;

    @Column(nullable = false)
    private String reason;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    private String notes;

    @PrePersist
    protected void onCreate() {
        returnDate = LocalDateTime.now();
    }
}
