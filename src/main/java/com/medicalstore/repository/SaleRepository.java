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

    List<Sale> findByCustomerId(Long customerId);

    List<Sale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(s.totalAmount) FROM Sale s WHERE s.saleDate BETWEEN ?1 AND ?2")
    Double getTotalSalesBetween(LocalDateTime start, LocalDateTime end);

    /** Returns the 5 most-recent sales — DB-level LIMIT, no Java streaming */
    List<Sale> findTop5ByOrderBySaleDateDesc();

    /** COUNT only — avoids fetching all Sale rows */
    long countBySaleDateBetween(LocalDateTime start, LocalDateTime end);

    /**
     * Single query with JOIN FETCH — avoids N+1 when loading full sale details.
     * Use this when the view needs medicine + customer data.
     */
    @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.medicine LEFT JOIN FETCH s.customer WHERE s.id = ?1")
    Optional<Sale> findByIdWithDetails(Long id);

    /**
     * Sales list with JOIN FETCH for a date range — one SQL statement instead of
     * 1+N.
     * Use this for reports that need medicine/customer data per sale.
     */
    @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.medicine LEFT JOIN FETCH s.customer " +
            "WHERE s.saleDate BETWEEN ?1 AND ?2 ORDER BY s.saleDate DESC")
    List<Sale> findBySaleDateBetweenWithDetails(LocalDateTime start, LocalDateTime end);
}
