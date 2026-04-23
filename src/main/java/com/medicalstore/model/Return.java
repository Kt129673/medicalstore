package com.medicalstore.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "returns", indexes = {
        @Index(name = "idx_return_sale", columnList = "sale_id"),
        @Index(name = "idx_return_date", columnList = "return_date")
})
@SQLDelete(sql = "UPDATE returns SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Return {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Version
    @Column(name = "version", nullable = false)
    private Long version = 0L;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    @JsonIgnore
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_item_id", nullable = false)
    @JsonIgnore
    private SaleItem saleItem;

    @Column(nullable = false)
    private Integer returnQuantity;

    @Column(nullable = false)
    private Double refundAmount;

    @Column(nullable = false)
    private String reason;

    @Column(name = "return_date")
    private LocalDateTime returnDate;

    private String notes;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @PrePersist
    protected void onCreate() {
        returnDate = LocalDateTime.now();
    }
}
