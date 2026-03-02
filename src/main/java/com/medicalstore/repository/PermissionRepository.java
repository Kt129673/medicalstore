package com.medicalstore.repository;

import com.medicalstore.model.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {

    Optional<Permission> findByCode(String code);

    boolean existsByCode(String code);

    /**
     * Returns all permission codes granted to the given role name.
     * Used by {@link com.medicalstore.service.PermissionService} for role-based caching.
     */
    @Query("SELECT p.code FROM Permission p WHERE :role MEMBER OF p.roles")
    Set<String> findCodesByRole(@Param("role") String role);

    /** All permissions that include at least one of the supplied roles. */
    @Query("SELECT p FROM Permission p WHERE :role MEMBER OF p.roles")
    List<Permission> findByRolesContaining(@Param("role") String role);
}
