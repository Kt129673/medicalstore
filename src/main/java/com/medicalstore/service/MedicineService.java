package com.medicalstore.service;

import com.medicalstore.model.Medicine;
import com.medicalstore.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicineService {

    private final MedicineRepository medicineRepository;

    // ── Context-Aware Lookups ───────────────────────────────────────────────
    public List<Medicine> getAllMedicines() {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        if (tenantId != null)
            return medicineRepository.findByBranchId(tenantId);
        if (ownerId != null)
            return medicineRepository.findByOwnerId(ownerId);
        return medicineRepository.findAll();
    }

    public long countAllMedicines() {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        if (tenantId != null)
            return medicineRepository.countByBranchId(tenantId);
        if (ownerId != null)
            return medicineRepository.countByOwnerId(ownerId);
        return medicineRepository.count();
    }

    public long countLowStockMedicines(Integer threshold) {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        if (tenantId != null)
            return medicineRepository.countByBranchIdAndQuantityLessThan(tenantId, threshold);
        if (ownerId != null)
            return medicineRepository.countLowStockByOwnerId(ownerId, threshold);
        return medicineRepository.countByQuantityLessThan(threshold);
    }

    public Optional<Medicine> getMedicineById(Long id) {
        // Find by ID, but verify it belongs to the tenant if context is set
        Optional<Medicine> medicine = medicineRepository.findById(id);
        if (medicine.isPresent()) {
            Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
            if (tenantId != null && !tenantId.equals(medicine.get().getBranch().getId())) {
                return Optional.empty(); // Not authorized
            }
            Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
            if (ownerId != null && !ownerId.equals(medicine.get().getBranch().getOwner().getId())) {
                return Optional.empty(); // Not authorized
            }
        }
        return medicine;
    }

    public List<Medicine> searchMedicines(String name) {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        if (tenantId != null)
            return medicineRepository.findByBranchIdAndNameContainingIgnoreCase(tenantId, name);
        // Owner global search not explicitly defined in repo, fallback to global for
        // MVP or implement custom
        return medicineRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Medicine> getLowStockMedicines(Integer threshold) {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        if (tenantId != null)
            return medicineRepository.findByBranchIdAndQuantityLessThan(tenantId, threshold);
        if (ownerId != null)
            return medicineRepository.findLowStockByOwnerId(ownerId, threshold);
        return medicineRepository.findByQuantityLessThan(threshold);
    }

    public List<Medicine> getExpiredMedicines() {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        if (tenantId != null)
            return medicineRepository.findByBranchIdAndExpiryDateBefore(tenantId, LocalDate.now());
        return medicineRepository.findByExpiryDateBefore(LocalDate.now());
    }

    public List<Medicine> getExpiringSoonMedicines(int days) {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        if (tenantId != null)
            return medicineRepository.findByBranchIdAndExpiryDateBetween(tenantId, LocalDate.now(),
                    LocalDate.now().plusDays(days));
        return medicineRepository.findByExpiryDateBetween(LocalDate.now(), LocalDate.now().plusDays(days));
    }

    public List<Medicine> getMedicinesByCategory(String category) {
        // Simplification for MVP: category search usually UI-filtered
        return medicineRepository.findByCategory(category);
    }

    // ── Branch-scoped (Legacy - kept for explicit calls if needed) ─────────

    // ── Writes (all roles, branch set by controller) ─────────────────────────
    @Transactional
    public Medicine saveMedicine(Medicine medicine) {
        if (medicine.getName() == null || medicine.getName().trim().isEmpty())
            throw new IllegalArgumentException("Medicine name is required");
        if (medicine.getCategory() == null || medicine.getCategory().trim().isEmpty())
            throw new IllegalArgumentException("Category is required");
        if (medicine.getPrice() == null || medicine.getPrice() <= 0)
            throw new IllegalArgumentException("Valid price is required");
        if (medicine.getQuantity() == null || medicine.getQuantity() < 0)
            throw new IllegalArgumentException("Valid quantity is required");
        if (medicine.getId() == null && medicine.getCreatedDate() == null)
            medicine.setCreatedDate(LocalDate.now());
        return medicineRepository.save(medicine);
    }

    @Transactional
    public void deleteMedicine(Long id) {
        medicineRepository.deleteById(id);
    }
}
