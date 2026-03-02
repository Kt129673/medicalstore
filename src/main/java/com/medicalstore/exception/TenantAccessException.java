package com.medicalstore.exception;

import org.springframework.security.access.AccessDeniedException;

/**
 * Thrown when a user attempts to access a resource belonging to a different tenant.
 * Extends AccessDeniedException so Spring Security's 403 handling still applies,
 * but carries a distinct type that GlobalExceptionHandler can log more precisely.
 * Maps to HTTP 403 Forbidden via GlobalExceptionHandler.
 */
public class TenantAccessException extends AccessDeniedException {

    private final String resource;
    private final Long attemptedTenantId;

    public TenantAccessException(String resource, Long attemptedTenantId) {
        super("Cross-tenant access denied for resource: " + resource);
        this.resource = resource;
        this.attemptedTenantId = attemptedTenantId;
    }

    public TenantAccessException(String message) {
        super(message);
        this.resource = null;
        this.attemptedTenantId = null;
    }

    public String getResource() {
        return resource;
    }

    public Long getAttemptedTenantId() {
        return attemptedTenantId;
    }
}
