package com.medicalstore.service;

import com.medicalstore.model.AuditLog;
import com.medicalstore.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Persists audit records to the {@code audit_logs} database table.
 *
 * <p>All methods are annotated {@code @Async} so audit writes never block the
 * main request thread. They are also annotated with
 * {@code Propagation.REQUIRES_NEW} so an audit write is committed regardless
 * of whether the calling transaction rolls back.</p>
 *
 * <p>Callers should only interact with this class through
 * {@link RoleAuditService}, which handles the SLF4J log entry first then
 * delegates here for DB persistence.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;

    /**
     * Persist an audit record asynchronously in its own transaction.
     *
     * @param entityName      Domain class name (e.g. {@code "User"})
     * @param entityId        PK of the affected entity, may be {@code null}
     * @param action          Action code (e.g. {@code "USER_CREATED"})
     * @param description     Human-readable summary
     * @param performedBy     Username of the actor
     * @param performedByRole Comma-separated role(s) of the actor
     * @param oldValue        JSON of old state (optional)
     * @param newValue        JSON of new state (optional)
     * @param ipAddress       Client IP address (optional)
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persist(
            String entityName,
            Long entityId,
            String action,
            String description,
            String performedBy,
            String performedByRole,
            String oldValue,
            String newValue,
            String ipAddress) {

        try {
            AuditLog record = AuditLog.builder()
                    .entityName(entityName)
                    .entityId(entityId)
                    .action(action)
                    .description(description)
                    .performedBy(performedBy)
                    .performedByRole(performedByRole)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .ipAddress(ipAddress)
                    .build();

            auditLogRepository.save(record);
        } catch (Exception ex) {
            // Audit failure must never propagate to the main request
            log.error("AUDIT_PERSIST_FAILURE | action={} | performer={} | error={}",
                    action, performedBy, ex.getMessage(), ex);
        }
    }

    /**
     * Convenience overload without IP address or value snapshots.
     */
    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void persist(
            String entityName,
            Long entityId,
            String action,
            String description,
            String performedBy,
            String performedByRole) {

        persist(entityName, entityId, action, description, performedBy, performedByRole,
                null, null, null);
    }

    // ── Read (synchronous, used by Admin audit-log page) ─────────────────────

    /**
     * Paginated search over audit logs with optional filters.
     * Used by {@code AdminController.auditLogs()} — eliminates
     * the direct {@code AuditLogRepository} injection in the controller.
     */
    @Transactional(readOnly = true)
    public Page<AuditLog> search(String username, String action,
            LocalDateTime from, LocalDateTime to, Pageable pageable) {
        return auditLogRepository.search(username, action, from, to, pageable);
    }

    /**
     * Returns the most recent 50 audit log entries for quick dashboard display.
     */
    @Transactional(readOnly = true)
    public java.util.List<AuditLog> getRecentLogs() {
        return auditLogRepository.findTop50ByOrderByTimestampDesc();
    }
}
