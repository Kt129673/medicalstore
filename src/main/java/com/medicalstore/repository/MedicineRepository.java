package com.medicalstore.repository;

import com.medicalstore.model.Medicine;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface MedicineRepository extends JpaRepository<Medicine, Long>, JpaSpecificationExecutor<Medicine> {

        @org.springframework.data.jpa.repository.Modifying
        @Query("UPDATE Medicine m SET m.quantity = m.quantity - :qty WHERE m.id = :id AND m.quantity >= :qty")
        int deductStock(@org.springframework.data.repository.query.Param("id") Long id,
                        @org.springframework.data.repository.query.Param("qty") int qty);

        @org.springframework.data.jpa.repository.Modifying
        @Query("UPDATE Medicine m SET m.quantity = m.quantity + :qty WHERE m.id = :id")
        int addStock(@org.springframework.data.repository.query.Param("id") Long id,
                        @org.springframework.data.repository.query.Param("qty") int qty);

        // --- existing ---
        Optional<Medicine> findByName(String name);

        @Query("SELECT DISTINCT m.category FROM Medicine m WHERE m.category IS NOT NULL ORDER BY m.category")
        List<String> findAllDistinctCategories();

        List<Medicine> findByCategory(String category);

        List<Medicine> findByQuantityLessThan(Integer quantity);

        @Query("SELECT DISTINCT m.category FROM Medicine m WHERE m.category IS NOT NULL AND m.category != '' ORDER BY m.category")
        List<String> findDistinctCategories();

        List<Medicine> findByExpiryDateBefore(LocalDate date);

        List<Medicine> findByExpiryDateBetween(LocalDate startDate, LocalDate endDate);

        Optional<Medicine> findByBarcode(String barcode);

        List<Medicine> findByNameContainingIgnoreCase(String name);

        long countByQuantityLessThan(Integer quantity);

        // --- branch-scoped (SHOPKEEPER) ---
        List<Medicine> findByBranchId(Long branchId);

        @Query("SELECT DISTINCT m.category FROM Medicine m WHERE m.branch.id = ?1 AND m.category IS NOT NULL ORDER BY m.category")
        List<String> findDistinctCategoriesByBranch(Long branchId);

        List<Medicine> findByBranchIdAndQuantityLessThan(Long branchId, Integer quantity);

        List<Medicine> findByBranchIdAndExpiryDateBefore(Long branchId, LocalDate date);

        List<Medicine> findByBranchIdAndExpiryDateBetween(Long branchId, LocalDate start, LocalDate end);

        List<Medicine> findByBranchIdAndNameContainingIgnoreCase(Long branchId, String name);

        long countByBranchId(Long branchId);

        long countByBranchIdAndQuantityLessThan(Long branchId, Integer quantity);

        // --- performance optimized (DTO & Caching) ---
        @Query("SELECT new com.medicalstore.dto.MedicineDTO(m.id, m.name, m.category, m.price, m.quantity, m.batchNumber, m.gstPercentage) "
                        +
                        "FROM Medicine m WHERE m.branch.id = :branchId AND (LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) "
                        +
                        "OR LOWER(m.barcode) = LOWER(:query) OR LOWER(m.batchNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
        List<com.medicalstore.dto.MedicineDTO> searchMedicinesDtoByBranch(Long branchId, String query,
                        org.springframework.data.domain.Pageable pageable);

        @Query("SELECT new com.medicalstore.dto.MedicineDTO(m.id, m.name, m.category, m.price, m.quantity, m.batchNumber, m.gstPercentage) "
                        +
                        "FROM Medicine m WHERE (LOWER(m.name) LIKE LOWER(CONCAT('%', :query, '%')) " +
                        "OR LOWER(m.barcode) = LOWER(:query) OR LOWER(m.batchNumber) LIKE LOWER(CONCAT('%', :query, '%')))")
        List<com.medicalstore.dto.MedicineDTO> searchMedicinesDtoGlobal(String query,
                        org.springframework.data.domain.Pageable pageable);

        // --- owner-scoped (OWNER sees all their branches) ---
        @Query("SELECT m FROM Medicine m WHERE m.branch.owner.id = :ownerId")
        List<Medicine> findByOwnerId(Long ownerId);

        @Query("SELECT DISTINCT m.category FROM Medicine m WHERE m.branch.owner.id = ?1 AND m.category IS NOT NULL ORDER BY m.category")
        List<String> findDistinctCategoriesByOwner(Long ownerId);

        @Query("SELECT m FROM Medicine m WHERE m.branch.owner.id = :ownerId AND m.quantity < :qty")
        List<Medicine> findLowStockByOwnerId(Long ownerId, Integer qty);

        @Query("SELECT COUNT(m) FROM Medicine m WHERE m.branch.owner.id = :ownerId")
        long countByOwnerId(Long ownerId);

        @Query("SELECT COUNT(m) FROM Medicine m WHERE m.branch.owner.id = :ownerId AND m.quantity < :qty")
        long countLowStockByOwnerId(Long ownerId, Integer qty);

        // --- global counts ---
        long count();

        // --- dashboard analytics (global / ADMIN) ---
        long countByExpiryDateBetween(LocalDate start, LocalDate end);

        List<Medicine> findTop5ByQuantityGreaterThanOrderByQuantityAsc(int minQty);

        @Query("SELECT m FROM Medicine m WHERE m.quantity <= ?1 AND m.quantity > 0 ORDER BY m.quantity ASC")
        List<Medicine> findCriticalLowStock(int threshold);

        // --- dashboard analytics (branch-scoped) ---
        long countByBranchIdAndExpiryDateBetween(Long branchId, LocalDate start, LocalDate end);

        @Query("SELECT m FROM Medicine m WHERE m.branch.id = ?1 AND m.quantity <= ?2 AND m.quantity > 0 ORDER BY m.quantity ASC")
        List<Medicine> findCriticalLowStockByBranch(Long branchId, int threshold);

        // --- dashboard analytics (owner-scoped) ---
        @Query("SELECT COUNT(m) FROM Medicine m WHERE m.branch.owner.id = :ownerId AND m.expiryDate BETWEEN :start AND :end")
        long countExpiringByOwner(Long ownerId, LocalDate start, LocalDate end);

        @Query("SELECT m FROM Medicine m WHERE m.branch.owner.id = ?1 AND m.quantity <= ?2 AND m.quantity > 0 ORDER BY m.quantity ASC")
        List<Medicine> findCriticalLowStockByOwner(Long ownerId, int threshold);

        // --- Advanced Analytics: Dead Stock ---

        /** Medicines NOT in given ID list and with stock > 0 (dead stock) */
        @Query("SELECT m FROM Medicine m WHERE m.id NOT IN :excludeIds AND m.quantity > 0 ORDER BY m.quantity DESC")
        List<Medicine> findDeadStock(List<Long> excludeIds);

        /** All medicines with stock > 0 (fallback when no sales exist) */
        List<Medicine> findByQuantityGreaterThan(int minQty);
}
