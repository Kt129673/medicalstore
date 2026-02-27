package com.medicalstore.repository;

import com.medicalstore.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long> {

    Optional<Medicine> findByName(String name);

    List<Medicine> findByCategory(String category);

    List<Medicine> findByQuantityLessThan(Integer quantity);

    List<Medicine> findByExpiryDateBefore(LocalDate date);

    List<Medicine> findByExpiryDateBetween(LocalDate startDate, LocalDate endDate);

    Optional<Medicine> findByBarcode(String barcode);

    List<Medicine> findByNameContainingIgnoreCase(String name);

    /** COUNT only — avoids fetching all Medicine rows */
    long countByQuantityLessThan(Integer quantity);
}
