package com.medicalstore.repository;

import com.medicalstore.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    /**
     * Same as {@link #findByUsername} but eagerly loads the {@code branch} association.
     * Use this in {@link com.medicalstore.config.TenantFilter} to avoid
     * {@code LazyInitializationException} after the repository transaction closes.
     */
    @EntityGraph(attributePaths = "branch")
    @Query("SELECT u FROM User u WHERE u.username = :username")
    Optional<User> findByUsernameWithBranch(@Param("username") String username);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    // ─── Role-based queries ────────────────────────────────────────────────────

    /** Find users that have a specific role (e.g., "OWNER", "SHOPKEEPER"). */
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r = :role ORDER BY u.fullName")
    List<User> findByRole(@Param("role") String role);

    /** Count users with a specific role. */
    @Query("SELECT COUNT(DISTINCT u) FROM User u JOIN u.roles r WHERE r = :role")
    long countByRole(@Param("role") String role);

    // ─── Branch-scoped queries ────────────────────────────────────────────────

    /** Find all shopkeepers assigned to a specific branch. */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.branch.id = :branchId AND r = 'SHOPKEEPER' ORDER BY u.fullName")
    List<User> findShopkeepersByBranchId(@Param("branchId") Long branchId);

    /** Find all shopkeepers assigned to branches owned by a given owner. */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE u.branch.owner.id = :ownerId AND r = 'SHOPKEEPER' ORDER BY u.fullName")
    List<User> findShopkeepersByOwnerId(@Param("ownerId") Long ownerId);

    // ─── Soft-delete support ──────────────────────────────────────────────────

    /**
     * Find soft-deleted users (bypasses @SQLRestriction using native SQL).
     * Used by the "Deleted Users" admin view.
     */
    @Query(value = "SELECT * FROM users WHERE is_deleted = true ORDER BY deleted_at DESC", nativeQuery = true)
    List<User> findDeletedUsers();

    /**
     * Restore a soft-deleted user by clearing the is_deleted flag.
     * Uses native SQL to bypass @SQLRestriction.
     */
    @Modifying
    @Query(value = "UPDATE users SET is_deleted = false, deleted_at = NULL WHERE id = :id", nativeQuery = true)
    void restoreUser(@Param("id") Long id);
}
