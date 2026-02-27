package com.medicalstore.repository;

import com.medicalstore.model.SupplierCredit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface SupplierCreditRepository extends JpaRepository<SupplierCredit, Long> {

    List<SupplierCredit> findBySupplierId(Long supplierId);

    List<SupplierCredit> findByBranchId(Long branchId);

    @Query("SELECT sc FROM SupplierCredit sc JOIN sc.branch b WHERE b.owner.id = :ownerId")
    List<SupplierCredit> findByOwnerId(@Param("ownerId") Long ownerId);

    @Query("SELECT sc FROM SupplierCredit sc WHERE sc.status != 'PAID' AND sc.dueDate < :date")
    List<SupplierCredit> findOverdueCredits(@Param("date") LocalDate date);

    @Query("SELECT sc FROM SupplierCredit sc WHERE sc.branch.id = :branchId AND sc.status != 'PAID' AND sc.dueDate < :date")
    List<SupplierCredit> findOverdueCreditsByBranch(@Param("branchId") Long branchId, @Param("date") LocalDate date);

    @Query("SELECT sc FROM SupplierCredit sc JOIN sc.branch b WHERE b.owner.id = :ownerId AND sc.status != 'PAID' AND sc.dueDate < :date")
    List<SupplierCredit> findOverdueCreditsByOwner(@Param("ownerId") Long ownerId, @Param("date") LocalDate date);

    // For aging report (0-30, 30-60, 60-90)
    // We will do the group-by/bucket logic in the Service layer to keep platform
    // independence
}
