package com.medicalstore.repository;

import com.medicalstore.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {

    // --- existing ---
    List<Sale> findByCustomerId(Long customerId);

    List<Sale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.saleDate BETWEEN ?1 AND ?2")
    Double getTotalSalesBetween(LocalDateTime start, LocalDateTime end);

    List<Sale> findTop5ByOrderBySaleDateDesc();

    long countBySaleDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.medicine LEFT JOIN FETCH s.customer WHERE s.id = ?1")
    Optional<Sale> findByIdWithDetails(Long id);

    @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.medicine LEFT JOIN FETCH s.customer " +
            "WHERE s.saleDate BETWEEN ?1 AND ?2 ORDER BY s.saleDate DESC")
    List<Sale> findBySaleDateBetweenWithDetails(LocalDateTime start, LocalDateTime end);

    // --- branch-scoped (SHOPKEEPER) ---
    List<Sale> findByBranchId(Long branchId);

    List<Sale> findTop5ByBranchIdOrderBySaleDateDesc(Long branchId);

    List<Sale> findByBranchIdAndSaleDateBetween(Long branchId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.branch.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3")
    Double getTotalSalesByBranchBetween(Long branchId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.medicine LEFT JOIN FETCH s.customer " +
            "WHERE s.branch.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3 ORDER BY s.saleDate DESC")
    List<Sale> findByBranchBetweenWithDetails(Long branchId, LocalDateTime start, LocalDateTime end);

    long countByBranchId(Long branchId);

    // --- owner-scoped (OWNER) ---
    @Query("SELECT s FROM Sale s WHERE s.branch.owner.id = ?1 ORDER BY s.saleDate DESC")
    List<Sale> findByOwnerId(Long ownerId);

    @Query("SELECT s FROM Sale s WHERE s.branch.owner.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3 ORDER BY s.saleDate DESC")
    List<Sale> findByOwnerIdBetween(Long ownerId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.branch.owner.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3")
    Double getTotalSalesByOwnerBetween(Long ownerId, LocalDateTime start, LocalDateTime end);

    @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.medicine LEFT JOIN FETCH s.customer " +
            "WHERE s.branch.owner.id = ?1 ORDER BY s.saleDate DESC")
    List<Sale> findTop5ByOwnerIdOrderBySaleDateDesc(Long ownerId);
}
