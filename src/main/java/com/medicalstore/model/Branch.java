package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "branches", indexes = {
        @Index(name = "idx_branch_owner", columnList = "owner_id"),
        @Index(name = "idx_branch_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Branch {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String address;

    private String phone;

    @Column(name = "gst_number")
    private String gstNumber;

    @Column(name = "licence_number")
    private String licenceNumber;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_date")
    private LocalDate createdDate;

    /** Owner of this branch (User with ROLE_OWNER) */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDate.now();
        if (isActive == null)
            isActive = true;
    }
}
