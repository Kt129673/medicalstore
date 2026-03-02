package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_user_branch",  columnList = "branch_id"),
        @Index(name = "idx_user_enabled", columnList = "enabled"),
        @Index(name = "idx_user_created", columnList = "created_date"),
        @Index(name = "idx_user_deleted", columnList = "is_deleted")
    }
)
@SQLDelete(sql = "UPDATE users SET is_deleted = true, deleted_at = NOW() WHERE id = ?")
@SQLRestriction("is_deleted = false")
@Data
@NoArgsConstructor
@AllArgsConstructor
@com.fasterxml.jackson.annotation.JsonIgnoreProperties({ "hibernateLazyInitializer", "handler" })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String fullName;

    @Column(unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private Boolean enabled = true;

    @Column(nullable = false)
    private Boolean accountNonLocked = true;

    @Column
    private LocalDateTime lastLogin;

    @Column
    private LocalDateTime createdDate;

    @Column
    private String createdBy;

    /** Soft-delete flag — set by Hibernate @SQLDelete. Never hard-deleted. */
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    /** Timestamp of soft deletion (null while active). */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    private Set<String> roles;

    /**
     * Assigned branch — only set for SHOPKEEPER role.
     * ADMIN and OWNER users have this null.
     * Fetched LAZILY — access inside a transaction or call Hibernate.initialize() explicitly.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "branch_id")
    @com.fasterxml.jackson.annotation.JsonIgnore
    private Branch branch;

    @PrePersist
    protected void onCreate() {
        createdDate = LocalDateTime.now();
    }
}
