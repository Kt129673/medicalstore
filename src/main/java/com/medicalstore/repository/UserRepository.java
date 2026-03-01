package com.medicalstore.repository;

import com.medicalstore.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

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
}
