package com.medicalstore.repository;

import com.medicalstore.model.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, Long> {

    List<Supplier> findByNameContainingIgnoreCase(String name);

    // --- branch-scoped (SHOPKEEPER) ---
    List<Supplier> findByBranchId(Long branchId);

    List<Supplier> findByBranchIdAndNameContainingIgnoreCase(Long branchId, String name);

    // --- owner-scoped (OWNER) ---
    @Query("SELECT s FROM Supplier s WHERE s.branch.owner.id = :ownerId")
    List<Supplier> findByOwnerId(Long ownerId);

    @Query("SELECT s FROM Supplier s WHERE s.branch.owner.id = :ownerId AND LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Supplier> findByOwnerIdAndNameContainingIgnoreCase(Long ownerId, String name);
}
