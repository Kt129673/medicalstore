package com.medicalstore.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Persistent audit log record.
 * Every sensitive action (user creation, deletion, subscription change, etc.)
 * is written here in addition to the SLF4J log.
 *
 * Populated asynchronously by {@link com.medicalstore.service.AuditLogService}
 * so the main request thread is never blocked.
 */
@Entity
@Table(
    name = "audit_logs",
    indexes = {
        @Index(name = "idx_audit_action",    columnList = "action"),
        @Index(name = "idx_audit_user",      columnList = "performed_by"),
        @Index(name = "idx_audit_time",      columnList = "timestamp"),
        @Index(name = "idx_audit_entity",    columnList = "entity_name,entity_id")
    }
)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Domain object type that was acted on (e.g. "User", "Branch", "SubscriptionPlan"). */
    @Column(name = "entity_name", length = 100)
    private String entityName;

    /** Primary key of the affected entity (nullable for non-entity actions). */
    @Column(name = "entity_id")
    private Long entityId;

    /** Action code — matches the constants used in {@link com.medicalstore.service.RoleAuditService}. */
    @Column(name = "action", length = 60, nullable = false)
    private String action;

    /** Username of the actor. */
    @Column(name = "performed_by", length = 100, nullable = false)
    private String performedBy;

    /** Role(s) of the actor at the time of the action (comma-separated). */
    @Column(name = "performed_by_role", length = 50)
    private String performedByRole;

    /** JSON snapshot of the entity state before the action (nullable). */
    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    /** JSON snapshot of the entity state after the action (nullable). */
    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    /** Human-readable description of the action. */
    @Column(name = "description", length = 500)
    private String description;

    /** Client IP address (best-effort, may be proxy IP). */
    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "timestamp", nullable = false, updatable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void prePersist() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
