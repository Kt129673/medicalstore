package com.medicalstore.config;

/**
 * @deprecated Moved to {@link com.medicalstore.common.TenantContext}.
 *             This stub exists only to ease migration; update usages to the new package.
 * @see com.medicalstore.common.TenantContext
 */
@Deprecated(since = "2.0", forRemoval = true)
public final class TenantContext {

    private TenantContext() {
    }

    public static void setTenantId(Long tenantId) { com.medicalstore.common.TenantContext.setTenantId(tenantId); }
    public static Long  getTenantId()              { return com.medicalstore.common.TenantContext.getTenantId(); }
    public static void  clearTenant()              { com.medicalstore.common.TenantContext.clearTenant(); }
    public static void  setOwnerId(Long ownerId)   { com.medicalstore.common.TenantContext.setOwnerId(ownerId); }
    public static Long  getOwnerId()               { return com.medicalstore.common.TenantContext.getOwnerId(); }
    public static void  clearOwner()               { com.medicalstore.common.TenantContext.clearOwner(); }
    public static void  clear()                    { com.medicalstore.common.TenantContext.clear(); }
}
