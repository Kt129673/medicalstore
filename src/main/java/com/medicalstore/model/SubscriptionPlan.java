package com.medicalstore.model;

import jakarta.persistence.*;
import jakarta.persistence.FetchType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Entity
@Table(name = "subscription_plans", indexes = {
        @Index(name = "idx_sub_expiry", columnList = "expiry_date"),
        @Index(name = "idx_sub_plan",   columnList = "plan_type"),
        @Index(name = "idx_sub_active", columnList = "active")
})
public class SubscriptionPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Associated with an Owner conceptually if multitenancy applies
    // but applying to Branch right now since Branches act as store tenants.
    // If the whole system is SaaS, "Tenant" is effectively the OWNER.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false)
    private String planType = "FREE"; // FREE, PRO, ENTERPRISE

    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(nullable = false)
    private Integer maxUsers = 1;

    @Column(nullable = false)
    private Integer maxBranches = 1;

    private boolean active = true;

    // Helper logic
    public boolean isExpired() {
        return LocalDate.now().isAfter(expiryDate);
    }

    public long getDaysUntilExpiry() {
        return LocalDate.now().until(expiryDate, java.time.temporal.ChronoUnit.DAYS);
    }
}
