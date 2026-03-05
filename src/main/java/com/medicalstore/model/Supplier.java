package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;

@Entity
@Table(name = "suppliers", indexes = {
        @Index(name = "idx_supplier_branch", columnList = "branch_id"),
        @Index(name = "idx_supplier_name", columnList = "name"),
        @Index(name = "idx_supplier_deleted", columnList = "is_deleted")
})
@SQLDelete(sql = "UPDATE suppliers SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contactPerson;

    @Column(nullable = false)
    private String phone;

    private String email;

    @Column(nullable = false)
    private String address;

    private String gstNumber;

    /** Branch this supplier belongs to */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    private Branch branch;

    /** Soft-delete flag — set by Hibernate @SQLDelete. */
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    /** Timestamp of soft deletion (null while active). */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
