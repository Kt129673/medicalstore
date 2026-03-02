package com.medicalstore.security;

import com.medicalstore.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * Spring Security {@link PermissionEvaluator} that wires MediStore's
 * role-permission table into the {@code hasPermission()} SpEL expression.
 *
 * <p>Usage in {@code @PreAuthorize}:
 * <pre>
 *   &#64;PreAuthorize("hasPermission(null, 'MEDICINE_DELETE')")
 *   &#64;PreAuthorize("hasPermission(null, 'REPORT_EXPORT_EXCEL')")
 * </pre>
 *
 * <p>The {@code targetDomainObject} argument is intentionally unused; pass
 * {@code null} or an optional entity ID for future domain-object scoping.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MediStorePermissionEvaluator implements PermissionEvaluator {

    private final PermissionService permissionService;

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String permCode = String.valueOf(permission);
        boolean granted = permissionService.hasPermission(authentication.getAuthorities(), permCode);
        if (!granted) {
            log.debug("PERMISSION DENIED: user={} permission={}", authentication.getName(), permCode);
        }
        return granted;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        // Delegate to the simpler overload — domain-object ACL is not used
        return hasPermission(authentication, (Object) null, permission);
    }
}
