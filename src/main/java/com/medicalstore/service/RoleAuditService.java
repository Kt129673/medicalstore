package com.medicalstore.service;

import com.medicalstore.model.User;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.common.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * RoleAuditService — tracks role-based actions for audit trail.
 *
 * Writes to both SLF4J (immediate, synchronous) and the
 * {@code audit_logs} database table (async via {@link AuditLogService}).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleAuditService {

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final AuditLogService auditLogService;

    /**
     * Log a sensitive action performed by a role.
     */
    public void logAction(String actionType, String description) {
        User currentUser = securityUtils.getCurrentUser();
        String username = currentUser != null ? currentUser.getUsername() : "ANONYMOUS";
        String roles = currentUser != null ? String.join(",", currentUser.getRoles()) : "UNKNOWN";

        log.info(
            "AUDIT_LOG | User: {} | Roles: {} | Action: {} | Description: {} | Timestamp: {}",
            username, roles, actionType, description, LocalDateTime.now()
        );

        auditLogService.persist(null, null, actionType, description, username, roles);
    }

    /**
     * Log when access is denied (403).
     */
    public void logAccessDenied(String attemptedPath, String reason) {
        User currentUser = securityUtils.getCurrentUser();
        String username = currentUser != null ? currentUser.getUsername() : "ANONYMOUS";
        String roles = currentUser != null ? String.join(",", currentUser.getRoles()) : "NONE";

        log.warn(
            "AUDIT_DENIED | User: {} | Roles: {} | Attempted: {} | Reason: {} | Timestamp: {}",
            username, roles, attemptedPath, reason, LocalDateTime.now()
        );

        auditLogService.persist(null, null, "AUDIT_DENIED",
                "Attempted: " + attemptedPath + " | Reason: " + reason, username, roles);
    }

    /**
     * Log role escalation attempts (trying to access another entity's data).
     */
    public void logEscalationAttempt(String endpoint, String userRole, String detail) {
        User currentUser = securityUtils.getCurrentUser();
        String username = currentUser != null ? currentUser.getUsername() : "ANONYMOUS";
        String roles = currentUser != null ? String.join(",", currentUser.getRoles()) : userRole;

        log.error(
            "PRIVILEGE_ESCALATION_ATTEMPT | User: {} | Role: {} | Endpoint: {} | Detail: {} | Timestamp: {}",
            username, userRole, endpoint, detail, LocalDateTime.now()
        );

        auditLogService.persist(null, null, "ESCALATION_ATTEMPT",
                "Endpoint: " + endpoint + " | Detail: " + detail, username, roles);
    }

    /**
     * Log admin creating or modifying another user.
     */
    public void logUserCreated(String username, String assignedRole) {
        logAction("USER_CREATED", String.format("Created user '%s' with role '%s'", username, assignedRole));
    }

    /**
     * Log admin disabling a user account.
     */
    public void logUserDisabled(String username) {
        logAction("USER_DISABLED", String.format("Disabled user account: %s", username));
    }

    /**
     * Log role change.
     */
    public void logRoleChanged(String username, String oldRole, String newRole) {
        logAction("ROLE_CHANGED", 
            String.format("Changed role for user '%s' from %s to %s", username, oldRole, newRole));
    }

    /**
     * Log admin resetting another user's password.
     */
    public void logPasswordReset(String targetUsername) {
        logAction("PASSWORD_RESET_BY_ADMIN", 
            String.format("Reset password for user: %s", targetUsername));
    }

    /**
     * Log subscription plan changes.
     */
    public void logSubscriptionChanged(Long ownerId, String oldPlan, String newPlan) {
        logAction("SUBSCRIPTION_CHANGED",
            String.format("Owner %d: Plan changed from %s to %s", ownerId, oldPlan, newPlan));
    }

    /**
     * Log branch creation/deletion (admin only).
     */
    public void logBranchOperations(String operation, String branchName) {
        logAction("BRANCH_" + operation, String.format("%s branch: %s", operation, branchName));
    }

    /**
     * Log data export operations.
     */
    public void logDataExport(String reportType) {
        logAction("DATA_EXPORT", String.format("Exported report: %s", reportType));
    }
}
