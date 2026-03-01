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

        @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.customer WHERE s.id = ?1")
        Optional<Sale> findByIdWithDetails(Long id);

        @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.customer " +
                        "WHERE s.saleDate BETWEEN ?1 AND ?2 ORDER BY s.saleDate DESC")
        List<Sale> findBySaleDateBetweenWithDetails(LocalDateTime start, LocalDateTime end);

        // --- branch-scoped (SHOPKEEPER) ---
        @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.customer " +
                        "WHERE s.branch.id = ?1 ORDER BY s.saleDate DESC")
        List<Sale> findByBranchId(Long branchId);

        @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.customer " +
                        "WHERE s.branch.id = ?1 ORDER BY s.saleDate DESC")
        List<Sale> findTop5ByBranchIdOrderBySaleDateDesc(Long branchId);

        @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.customer " +
                        "WHERE s.branch.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3 ORDER BY s.saleDate DESC")
        List<Sale> findByBranchIdAndSaleDateBetween(Long branchId, LocalDateTime start, LocalDateTime end);

        @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.branch.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3")
        Double getTotalSalesByBranchBetween(Long branchId, LocalDateTime start, LocalDateTime end);

        @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.customer " +
                        "WHERE s.branch.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3 ORDER BY s.saleDate DESC")
        List<Sale> findByBranchBetweenWithDetails(Long branchId, LocalDateTime start, LocalDateTime end);

        long countByBranchId(Long branchId);

        // --- owner-scoped (OWNER sees all branches belonging to them) ---
        @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.customer " +
                        "WHERE s.branch.owner.id = ?1 ORDER BY s.saleDate DESC")
        List<Sale> findByOwnerId(Long ownerId);

        @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.customer " +
                        "WHERE s.branch.owner.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3 ORDER BY s.saleDate DESC")
        List<Sale> findByOwnerIdBetween(Long ownerId, LocalDateTime start, LocalDateTime end);

        @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.branch.owner.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3")
        Double getTotalSalesByOwnerBetween(Long ownerId, LocalDateTime start, LocalDateTime end);

        @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.customer " +
                        "WHERE s.branch.owner.id = ?1 ORDER BY s.saleDate DESC")
        List<Sale> findTop5ByOwnerIdOrderBySaleDateDesc(Long ownerId);

        // --- pagination methods ---
        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "customer" })
        org.springframework.data.domain.Page<Sale> findAllByOrderBySaleDateDesc(
                        org.springframework.data.domain.Pageable pageable);

        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "customer" })
        org.springframework.data.domain.Page<Sale> findByBranchIdOrderBySaleDateDesc(Long branchId,
                        org.springframework.data.domain.Pageable pageable);

        @org.springframework.data.jpa.repository.EntityGraph(attributePaths = { "customer", "branch" })
        org.springframework.data.domain.Page<Sale> findByBranchOwnerIdOrderBySaleDateDesc(Long ownerId,
                        org.springframework.data.domain.Pageable pageable);

        // --- dashboard analytics (global / ADMIN) ---
        @Query("SELECT CAST(s.saleDate AS LocalDate), COALESCE(SUM(s.finalAmount), SUM(s.totalAmount)) " +
                        "FROM Sale s WHERE s.saleDate >= ?1 GROUP BY CAST(s.saleDate AS LocalDate) ORDER BY CAST(s.saleDate AS LocalDate)")
        List<Object[]> getDailySalesTotals(LocalDateTime since);

        @Query("SELECT i.medicine.category, SUM(i.totalPrice) " +
                        "FROM Sale s JOIN s.items i WHERE s.saleDate BETWEEN ?1 AND ?2 GROUP BY i.medicine.category ORDER BY SUM(i.totalPrice) DESC")
        List<Object[]> getSalesByCategory(LocalDateTime start, LocalDateTime end);

        @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.customer ORDER BY s.saleDate DESC")
        List<Sale> findTop10WithDetails();

        @Query("SELECT COUNT(s) FROM Sale s WHERE s.saleDate BETWEEN ?1 AND ?2")
        long countSalesBetween(LocalDateTime start, LocalDateTime end);

        @Query("SELECT COALESCE(SUM(s.finalAmount), SUM(s.totalAmount)) FROM Sale s WHERE s.saleDate BETWEEN ?1 AND ?2")
        Double getRevenueBetween(LocalDateTime start, LocalDateTime end);

        // --- dashboard analytics (branch-scoped / SHOPKEEPER) ---
        @Query("SELECT CAST(s.saleDate AS LocalDate), COALESCE(SUM(s.finalAmount), SUM(s.totalAmount)) " +
                        "FROM Sale s WHERE s.branch.id = ?1 AND s.saleDate >= ?2 GROUP BY CAST(s.saleDate AS LocalDate) ORDER BY CAST(s.saleDate AS LocalDate)")
        List<Object[]> getDailySalesTotalsByBranch(Long branchId, LocalDateTime since);

        @Query("SELECT i.medicine.category, SUM(i.totalPrice) " +
                        "FROM Sale s JOIN s.items i WHERE s.branch.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3 GROUP BY i.medicine.category ORDER BY SUM(i.totalPrice) DESC")
        List<Object[]> getSalesByCategoryByBranch(Long branchId, LocalDateTime start, LocalDateTime end);

        @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.customer " +
                        "WHERE s.branch.id = ?1 ORDER BY s.saleDate DESC")
        List<Sale> findTop10WithDetailsByBranch(Long branchId);

        // --- dashboard analytics (owner-scoped / OWNER) ---
        @Query("SELECT CAST(s.saleDate AS LocalDate), COALESCE(SUM(s.finalAmount), SUM(s.totalAmount)) " +
                        "FROM Sale s WHERE s.branch.owner.id = ?1 AND s.saleDate >= ?2 GROUP BY CAST(s.saleDate AS LocalDate) ORDER BY CAST(s.saleDate AS LocalDate)")
        List<Object[]> getDailySalesTotalsByOwner(Long ownerId, LocalDateTime since);

        @Query("SELECT i.medicine.category, SUM(i.totalPrice) " +
                        "FROM Sale s JOIN s.items i WHERE s.branch.owner.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3 GROUP BY i.medicine.category ORDER BY SUM(i.totalPrice) DESC")
        List<Object[]> getSalesByCategoryByOwner(Long ownerId, LocalDateTime start, LocalDateTime end);

        // --- Advanced Analytics queries ---

        /**
         * Profit per medicine: [medicineId, name, category, totalRevenue, totalCost,
         * totalProfit, qtySold]
         */
        @Query("SELECT m.id, m.name, m.category, " +
                        "SUM(i.totalPrice), " +
                        "SUM(i.costPrice * i.quantity), " +
                        "SUM(i.totalPrice - (i.costPrice * i.quantity)), " +
                        "SUM(i.quantity) " +
                        "FROM Sale s JOIN s.items i JOIN i.medicine m WHERE s.saleDate BETWEEN ?1 AND ?2 " +
                        "GROUP BY m.id, m.name, m.category " +
                        "ORDER BY SUM(i.totalPrice - (i.costPrice * i.quantity)) DESC")
        List<Object[]> getProfitPerMedicine(LocalDateTime start, LocalDateTime end);

        /**
         * Fast-moving Top N: [medicineId, name, category, totalQtySold, totalRevenue]
         */
        @Query("SELECT m.id, m.name, m.category, SUM(i.quantity), SUM(i.totalPrice) " +
                        "FROM Sale s JOIN s.items i JOIN i.medicine m WHERE s.saleDate BETWEEN ?1 AND ?2 " +
                        "GROUP BY m.id, m.name, m.category ORDER BY SUM(i.quantity) DESC")
        List<Object[]> getTopSellingMedicines(LocalDateTime start, LocalDateTime end);

        /**
         * Medicine IDs with at least one sale since given date (for dead-stock
         * exclusion)
         */
        @Query("SELECT DISTINCT i.medicine.id FROM Sale s JOIN s.items i WHERE s.saleDate >= ?1")
        List<Long> getMedicineIdsWithSalesSince(LocalDateTime since);

        /**
         * Monthly GST breakdown: [yearMonth (string), totalTaxable, totalCGST,
         * totalSGST, totalGST, transactionCount]
         */
        @Query("SELECT FUNCTION('DATE_FORMAT', s.saleDate, '%Y-%m'), " +
                        "SUM(s.totalAmount - COALESCE(s.discountAmount, 0.0)), " +
                        "SUM(COALESCE(s.gstAmount, 0.0)) / 2, " +
                        "SUM(COALESCE(s.gstAmount, 0.0)) / 2, " +
                        "SUM(COALESCE(s.gstAmount, 0.0)), " +
                        "COUNT(s) " +
                        "FROM Sale s WHERE s.saleDate BETWEEN ?1 AND ?2 " +
                        "GROUP BY FUNCTION('DATE_FORMAT', s.saleDate, '%Y-%m') " +
                        "ORDER BY FUNCTION('DATE_FORMAT', s.saleDate, '%Y-%m')")
        List<Object[]> getMonthlyGstSummary(LocalDateTime start, LocalDateTime end);
        // ── Eager item loading (avoids N+1 in reports) ─────────────────────────

        /** Load sales with their items and medicines in a single JOIN FETCH — used by reports */
        @Query("SELECT DISTINCT s FROM Sale s " +
                        "LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.medicine " +
                        "WHERE s.saleDate BETWEEN ?1 AND ?2 ORDER BY s.saleDate DESC")
        List<Sale> findWithItemsBySaleDateBetween(LocalDateTime start, LocalDateTime end);

        @Query("SELECT DISTINCT s FROM Sale s " +
                        "LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.medicine " +
                        "WHERE s.branch.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3 ORDER BY s.saleDate DESC")
        List<Sale> findWithItemsByBranchBetween(Long branchId, LocalDateTime start, LocalDateTime end);

        @Query("SELECT DISTINCT s FROM Sale s " +
                        "LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.medicine " +
                        "WHERE s.branch.owner.id = ?1 AND s.saleDate BETWEEN ?2 AND ?3 ORDER BY s.saleDate DESC")
        List<Sale> findWithItemsByOwnerBetween(Long ownerId, LocalDateTime start, LocalDateTime end);

        /**
         * Top-selling medicines with SQL LIMIT pushed via Pageable — avoids fetching
         * ALL rows into Java and breaking in a loop.
         */
        @Query("SELECT m.id, m.name, m.category, SUM(i.quantity), SUM(i.totalPrice) " +
                        "FROM Sale s JOIN s.items i JOIN i.medicine m WHERE s.saleDate BETWEEN ?1 AND ?2 " +
                        "GROUP BY m.id, m.name, m.category ORDER BY SUM(i.quantity) DESC")
        List<Object[]> getTopSellingMedicinesLimited(LocalDateTime start, LocalDateTime end,
                        org.springframework.data.domain.Pageable pageable);}
