package com.medicalstore.repository;

import com.medicalstore.model.Return;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReturnRepository extends JpaRepository<Return, Long> {
    
    List<Return> findBySaleId(Long saleId);

    // --- branch-scoped (SHOPKEEPER) ---
    @Query("SELECT r FROM Return r JOIN r.sale s WHERE s.branch.id = :branchId ORDER BY r.returnDate DESC")
    List<Return> findByBranchId(Long branchId);

    // --- owner-scoped (OWNER) ---
    @Query("SELECT r FROM Return r JOIN r.sale s WHERE s.branch.owner.id = :ownerId ORDER BY r.returnDate DESC")
    List<Return> findByOwnerId(Long ownerId);
}
