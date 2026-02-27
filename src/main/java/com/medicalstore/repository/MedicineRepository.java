package com.medicalstore.repository;

import com.medicalstore.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    // --- existing ---
    Optional<Medicine> findByName(String name);

    List<Medicine> findByCategory(String category);

    List<Medicine> findByQuantityLessThan(Integer quantity);

    List<Medicine> findByExpiryDateBefore(LocalDate date);

    List<Medicine> findByExpiryDateBetween(LocalDate startDate, LocalDate endDate);

    Optional<Medicine> findByBarcode(String barcode);

    List<Medicine> findByNameContainingIgnoreCase(String name);

    long countByQuantityLessThan(Integer quantity);

    // --- branch-scoped (SHOPKEEPER) ---
    List<Medicine> findByBranchId(Long branchId);

    List<Medicine> findByBranchIdAndQuantityLessThan(Long branchId, Integer quantity);

    List<Medicine> findByBranchIdAndExpiryDateBefore(Long branchId, LocalDate date);

    List<Medicine> findByBranchIdAndExpiryDateBetween(Long branchId, LocalDate start, LocalDate end);

    List<Medicine> findByBranchIdAndNameContainingIgnoreCase(Long branchId, String name);

    long countByBranchId(Long branchId);

    long countByBranchIdAndQuantityLessThan(Long branchId, Integer quantity);

    // --- owner-scoped (OWNER sees all their branches) ---
    @Query("SELECT m FROM Medicine m WHERE m.branch.owner.id = :ownerId")
    List<Medicine> findByOwnerId(Long ownerId);

    @Query("SELECT m FROM Medicine m WHERE m.branch.owner.id = :ownerId AND m.quantity < :qty")
    List<Medicine> findLowStockByOwnerId(Long ownerId, Integer qty);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.branch.owner.id = :ownerId")
    long countByOwnerId(Long ownerId);

    @Query("SELECT COUNT(m) FROM Medicine m WHERE m.branch.owner.id = :ownerId AND m.quantity < :qty")
    long countLowStockByOwnerId(Long ownerId, Integer qty);

    // --- global counts ---
    long count();
}
