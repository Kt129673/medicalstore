package com.medicalstore.common;

/**
 * ThreadLocal context to hold the current tenant (Branch ID) and owner (User ID)
 * for the active request.
 *
 * <p>Set by {@code TenantFilter} at the start of every request and cleared in
 * the finally block to prevent leaks across thread-pool reuse.
 */
public class TenantContext {

    private static final ThreadLocal<Long> CURRENT_TENANT = new ThreadLocal<>();
    private static final ThreadLocal<Long> CURRENT_OWNER  = new ThreadLocal<>();

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) {
        CURRENT_TENANT.set(tenantId);
    }

    public static Long getTenantId() {
        return CURRENT_TENANT.get();
    }

    public static void clearTenant() {
        CURRENT_TENANT.remove();
    }

    public static void setOwnerId(Long ownerId) {
        CURRENT_OWNER.set(ownerId);
    }

    public static Long getOwnerId() {
        return CURRENT_OWNER.get();
    }

    public static void clearOwner() {
        CURRENT_OWNER.remove();
    }

    /** Clears both tenant and owner from the current thread. */
    public static void clear() {
        CURRENT_TENANT.remove();
        CURRENT_OWNER.remove();
    }
}
