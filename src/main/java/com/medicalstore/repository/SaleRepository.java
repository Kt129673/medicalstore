package com.medicalstore.repository;

import com.medicalstore.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

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
}
