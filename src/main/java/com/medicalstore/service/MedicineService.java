package com.medicalstore.service;

import com.medicalstore.model.Medicine;
import com.medicalstore.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineService {

    private final MedicineRepository medicineRepository;

    // ── Context-Aware Lookups ───────────────────────────────────────────────
    public java.util.List<String> getAllCategories() {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();
        if (tenantId != null)
            return medicineRepository.findDistinctCategoriesByBranch(tenantId);
        if (ownerId != null)
            return medicineRepository.findDistinctCategoriesByOwner(ownerId);
        return medicineRepository.findAllDistinctCategories();
    }

    public org.springframework.data.domain.Page<Medicine> filterMedicines(
            String search, String category, String stockLevel, String expiryRange,
            org.springframework.data.domain.Pageable pageable) {

        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();

        org.springframework.data.jpa.domain.Specification<Medicine> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            if (tenantId != null) {
                predicates.add(cb.equal(root.get("branch").get("id"), tenantId));
            } else if (ownerId != null) {
                predicates.add(cb.equal(root.get("branch").get("owner").get("id"), ownerId));
            }

            if (search != null && !search.isBlank()) {
                String likePattern = "%" + search.toLowerCase(java.util.Locale.ROOT) + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("name")), likePattern),
                        cb.like(cb.lower(root.get("barcode")), likePattern),
                        cb.like(cb.lower(root.get("manufacturer")), likePattern)));
            }

            if (category != null && !category.isBlank()) {
                predicates.add(cb.equal(root.get("category"), category));
            }

            if (stockLevel != null && !stockLevel.isBlank()) {
                if ("critical".equals(stockLevel)) {
                    predicates.add(cb.le(root.get("quantity"), 5));
                } else if ("low".equals(stockLevel)) {
                    predicates.add(cb.between(root.get("quantity"), 6, 10));
                } else if ("ok".equals(stockLevel)) {
                    predicates.add(cb.gt(root.get("quantity"), 10));
                }
            }

            if (expiryRange != null && !expiryRange.isBlank()) {
                java.time.LocalDate now = java.time.LocalDate.now();
                if ("expired".equals(expiryRange)) {
                    predicates.add(cb.lessThan(root.get("expiryDate"), now));
                } else if ("30".equals(expiryRange)) {
                    predicates.add(cb.between(root.get("expiryDate"), now, now.plusDays(30)));
                } else if ("90".equals(expiryRange)) {
                    predicates.add(cb.between(root.get("expiryDate"), now, now.plusDays(90)));
                }
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return medicineRepository.findAll(spec, pageable);
    }

    public List<Medicine> getAllMedicines() {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();

        if (tenantId != null)
            return medicineRepository.findByBranchId(tenantId);
        if (ownerId != null)
            return medicineRepository.findByOwnerId(ownerId);
        return medicineRepository.findAll();
    }

    public long countAllMedicines() {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();

        if (tenantId != null)
            return medicineRepository.countByBranchId(tenantId);
        if (ownerId != null)
            return medicineRepository.countByOwnerId(ownerId);
        return medicineRepository.count();
    }

    public long countLowStockMedicines(Integer threshold) {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();

        if (tenantId != null)
            return medicineRepository.countByBranchIdAndQuantityLessThan(tenantId, threshold);
        if (ownerId != null)
            return medicineRepository.countLowStockByOwnerId(ownerId, threshold);
        return medicineRepository.countByQuantityLessThan(threshold);
    }

    public Optional<Medicine> getMedicineById(Long id) {
        // Find by ID, but verify it belongs to the tenant or owner if context is set
        Optional<Medicine> medicine = medicineRepository.findById(id);
        if (medicine.isPresent()) {
            Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
            if (tenantId != null && medicine.get().getBranch() != null
                    && !tenantId.equals(medicine.get().getBranch().getId())) {
                // Throw rather than silently return empty — consistent with all other services
                throw new org.springframework.security.access.AccessDeniedException(
                        "Access denied: medicine belongs to a different branch");
            }
            Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();
            if (ownerId != null && medicine.get().getBranch() != null
                    && medicine.get().getBranch().getOwner() != null
                    && !ownerId.equals(medicine.get().getBranch().getOwner().getId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Access denied: medicine belongs to a different owner");
            }
        }
        return medicine;
    }

    public List<Medicine> searchMedicines(String name) {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        if (tenantId != null)
            return medicineRepository.findByBranchIdAndNameContainingIgnoreCase(tenantId, name);
        // Owner global search not explicitly defined in repo, fallback to global for
        // MVP or implement custom
        return medicineRepository.findByNameContainingIgnoreCase(name);
    }

    // --- Performance optimized search for POS ---
    @org.springframework.cache.annotation.Cacheable(value = "medicines_search", key = "#query + '-' + T(com.medicalstore.common.TenantContext).getTenantId()")
    public List<com.medicalstore.dto.MedicineDTO> searchMedicinesForPos(String query) {
        org.springframework.data.domain.Pageable top20 = org.springframework.data.domain.PageRequest.of(0, 20);
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        if (tenantId != null) {
            return medicineRepository.searchMedicinesDtoByBranch(tenantId, query, top20);
        }
        return medicineRepository.searchMedicinesDtoGlobal(query, top20);
    }

    public List<Medicine> getLowStockMedicines(Integer threshold) {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();

        if (tenantId != null)
            return medicineRepository.findByBranchIdAndQuantityLessThan(tenantId, threshold);
        if (ownerId != null)
            return medicineRepository.findLowStockByOwnerId(ownerId, threshold);
        return medicineRepository.findByQuantityLessThan(threshold);
    }

    public List<Medicine> getExpiredMedicines() {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        if (tenantId != null)
            return medicineRepository.findByBranchIdAndExpiryDateBefore(tenantId, LocalDate.now());
        return medicineRepository.findByExpiryDateBefore(LocalDate.now());
    }

    public List<Medicine> getExpiringSoonMedicines(int days) {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        if (tenantId != null)
            return medicineRepository.findByBranchIdAndExpiryDateBetween(tenantId, LocalDate.now(),
                    LocalDate.now().plusDays(days));
        return medicineRepository.findByExpiryDateBetween(LocalDate.now(), LocalDate.now().plusDays(days));
    }

    /**
     * COUNT-only query — avoids loading full Medicine entities just to call .size()
     */
    public long countExpiringSoonMedicines(int days) {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();
        LocalDate now = LocalDate.now();
        LocalDate limit = now.plusDays(days);
        if (tenantId != null)
            return medicineRepository.countByBranchIdAndExpiryDateBetween(tenantId, now, limit);
        if (ownerId != null)
            return medicineRepository.countExpiringByOwner(ownerId, now, limit);
        return medicineRepository.countByExpiryDateBetween(now, limit);
    }

    public List<Medicine> getMedicinesByCategory(String category) {
        // Simplification for MVP: category search usually UI-filtered
        return medicineRepository.findByCategory(category);
    }

    /**
     * Explicitly fetch medicines for a specific branch (used by Owner branch
     * detail).
     */
    public List<Medicine> getMedicinesByBranch(Long branchId) {
        return medicineRepository.findByBranchId(branchId);
    }

    // ── Branch-scoped (Legacy - kept for explicit calls if needed) ─────────

    // ── Writes (all roles, branch set by controller) ——————————————————————————
    @Transactional
    @CacheEvict(value = "medicines_search", allEntries = true)
    public Medicine saveMedicine(Medicine medicine) {
        if (medicine.getName() == null || medicine.getName().isBlank())
            throw new IllegalArgumentException("Medicine name is required");
        if (medicine.getCategory() == null || medicine.getCategory().isBlank())
            throw new IllegalArgumentException("Category is required");
        if (medicine.getPrice() == null || medicine.getPrice() <= 0)
            throw new IllegalArgumentException("Valid price is required");
        if (medicine.getQuantity() == null || medicine.getQuantity() < 0)
            throw new IllegalArgumentException("Valid quantity is required");
        if (medicine.getBarcode() == null || medicine.getBarcode().isBlank()) {
            medicine.setBarcode(
                    "BAR-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase(java.util.Locale.ROOT));
        }
        if (medicine.getId() == null && medicine.getCreatedDate() == null)
            medicine.setCreatedDate(LocalDate.now());
        return medicineRepository.save(medicine);
    }

    @Transactional
    @CacheEvict(value = "medicines_search", allEntries = true)
    public void deleteMedicine(Long id) {
        medicineRepository.deleteById(id);
    }
}
