package com.medicalstore.repository;

import com.medicalstore.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    @Query("SELECT b FROM Branch b JOIN FETCH b.owner WHERE b.owner.id = :ownerId")
    List<Branch> findByOwnerId(Long ownerId);

    @Query("SELECT b FROM Branch b JOIN FETCH b.owner WHERE b.owner.id = :ownerId AND b.isActive = true")
    List<Branch> findByOwnerIdAndIsActiveTrue(Long ownerId);

    @Query("SELECT b FROM Branch b JOIN FETCH b.owner WHERE b.isActive = true")
    List<Branch> findByIsActiveTrue();

    @Query("SELECT b FROM Branch b JOIN FETCH b.owner")
    List<Branch> findAll();

    long countByOwnerId(Long ownerId);
    
    // ─── Branch Comparison Optimized Queries ───────────────────────────────
    
    /**
     * Get basic branch info for comparison (used as base DTO).
     * Returns: [branchId, name, address, isActive]
     */
    @Query("SELECT new com.medicalstore.dto.BranchComparisonDTO(b.id, b.name, b.address, b.isActive) " +
           "FROM Branch b WHERE b.owner.id = :ownerId ORDER BY b.name")
    List<com.medicalstore.dto.BranchComparisonDTO> getBranchComparisonBase(Long ownerId);
    
    /**
     * Get medicine counts per branch for an owner.
     * Returns: [branchId, count]
     */
    @Query("SELECT m.branch.id, COUNT(m) FROM com.medicalstore.model.Medicine m " +
           "WHERE m.branch.owner.id = :ownerId GROUP BY m.branch.id")
    List<Object[]> getMedicineCountsByOwner(Long ownerId);
    
    /**
     * Get low stock counts per branch (quantity < 10).
     * Returns: [branchId, count]
     */
    @Query("SELECT m.branch.id, COUNT(m) FROM com.medicalstore.model.Medicine m " +
           "WHERE m.branch.owner.id = :ownerId AND m.quantity < 10 GROUP BY m.branch.id")
    List<Object[]> getLowStockCountsByOwner(Long ownerId);
    
    /**
     * Get expiring medicine counts per branch (30 days).
     * Returns: [branchId, count]
     */
    @Query("SELECT m.branch.id, COUNT(m) FROM com.medicalstore.model.Medicine m " +
           "WHERE m.branch.owner.id = :ownerId AND m.expiryDate BETWEEN :startDate AND :endDate " +
           "GROUP BY m.branch.id")
    List<Object[]> getExpiringCountsByOwner(Long ownerId, java.time.LocalDate startDate, java.time.LocalDate endDate);
    
    /**
     * Get total shopkeepers per branch.
     * Returns: [branchId, count]
     */
    @Query("SELECT u.branch.id, COUNT(u) FROM com.medicalstore.model.User u " +
           "JOIN u.roles r WHERE u.branch.owner.id = :ownerId AND r = 'SHOPKEEPER' " +
           "GROUP BY u.branch.id")
    List<Object[]> getShopkeeperCountsByOwner(Long ownerId);
    
    /**
     * Get active shopkeepers per branch (isActive = true).
     * Returns: [branchId, count]
     */
    @Query("SELECT u.branch.id, COUNT(u) FROM com.medicalstore.model.User u " +
           "JOIN u.roles r WHERE u.branch.owner.id = :ownerId AND r = 'SHOPKEEPER' " +
           "AND u.isActive = true GROUP BY u.branch.id")
    List<Object[]> getActiveShopkeeperCountsByOwner(Long ownerId);
    
    /**
     * Get customer counts per branch.
     * Returns: [branchId, count]
     */
    @Query("SELECT c.branch.id, COUNT(c) FROM com.medicalstore.model.Customer c " +
           "WHERE c.branch.owner.id = :ownerId GROUP BY c.branch.id")
    List<Object[]> getCustomerCountsByOwner(Long ownerId);
    
    /**
     * Get today's sales per branch.
     * Returns: [branchId, sum(totalAmount)]
     */
    @Query("SELECT s.branch.id, COALESCE(SUM(s.finalAmount), SUM(s.totalAmount)) " +
           "FROM com.medicalstore.model.Sale s " +
           "WHERE s.branch.owner.id = :ownerId AND s.saleDate BETWEEN :startDate AND :endDate " +
           "GROUP BY s.branch.id")
    List<Object[]> getSalesTotalsByOwner(Long ownerId, java.time.LocalDateTime startDate, java.time.LocalDateTime endDate);
}
