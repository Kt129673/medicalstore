package com.medicalstore.service;

import com.medicalstore.model.Permission;
import com.medicalstore.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

/**
 * Provides role → permission code lookups, backed by a Caffeine cache.
 *
 * <p>Cache name: {@code role_permissions}. Entries are keyed on the
 * role name string (e.g. "ADMIN", "OWNER", "SHOPKEEPER"). The cache
 * is evicted whenever permissions are seeded or updated by admin tooling.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PermissionService {

    private final PermissionRepository permissionRepository;

    /**
     * Returns the set of permission codes granted to {@code roleName}.
     * Results are cached so repeated lookups during a request are free.
     *
     * @param roleName role without the ROLE_ prefix (e.g. "ADMIN")
     */
    @Cacheable(value = "role_permissions", key = "#roleName")
    @Transactional(readOnly = true)
    public Set<String> getPermissionsForRole(String roleName) {
        Set<String> codes = permissionRepository.findCodesByRole(roleName);
        log.debug("Loaded {} permission(s) for role {}", codes.size(), roleName);
        return codes;
    }

    /**
     * Checks whether ANY of the caller's Spring Security authorities
     * (role names) are granted the given permission code.
     *
     * @param authorities collection of authority strings (e.g. "ROLE_ADMIN")
     * @param permissionCode the code to check (e.g. "MEDICINE_DELETE")
     */
    public boolean hasPermission(java.util.Collection<? extends org.springframework.security.core.GrantedAuthority> authorities,
                                  String permissionCode) {
        for (var ga : authorities) {
            // Strip ROLE_ prefix to match our storage format
            String roleName = ga.getAuthority().startsWith("ROLE_")
                    ? ga.getAuthority().substring(5)
                    : ga.getAuthority();
            Set<String> codes = getPermissionsForRole(roleName);
            if (codes.contains(permissionCode)) {
                return true;
            }
        }
        return false;
    }

    /** Evict the cache for all roles — call after seeding/updating permissions. */
    @CacheEvict(value = "role_permissions", allEntries = true)
    public void evictAll() {
        log.info("Evicted role_permissions cache");
    }
}
