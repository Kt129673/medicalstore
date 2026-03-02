package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a fine-grained permission code (e.g. MEDICINE_DELETE, REPORT_EXPORT_EXCEL).
 * Roles are stored as a simple collection of strings so no separate Role entity is required.
 * The role hierarchy defined in SecurityConfig still applies on top of these codes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "permissions", indexes = {
        @Index(name = "idx_perm_code", columnList = "code", unique = true)
})
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Unique permission code — used in @PreAuthorize("hasPermission(null, 'CODE')") */
    @Column(unique = true, nullable = false, length = 100)
    private String code;

    @Column(length = 255)
    private String description;

    /**
     * The set of role names (without ROLE_ prefix) that are granted this permission.
     * Hierarchy is NOT applied here — explicit role assignment only.
     * Admin actions should include all three roles when appropriate.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            name = "role_permissions",
            joinColumns = @JoinColumn(name = "permission_id"),
            indexes = @Index(name = "idx_rp_role", columnList = "role_name")
    )
    @Column(name = "role_name", length = 50)
    @Builder.Default
    private Set<String> roles = new HashSet<>();
}
