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

    // ── Global (ADMIN) ──────────────────────────────────────────────────────
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public long countAllMedicines() {
        return medicineRepository.count();
    }

    public long countLowStockMedicines(Integer t) {
        return medicineRepository.countByQuantityLessThan(t);
    }

    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }

    public List<Medicine> searchMedicines(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Medicine> getLowStockMedicines(Integer threshold) {
        return medicineRepository.findByQuantityLessThan(threshold);
    }

    public List<Medicine> getExpiredMedicines() {
        return medicineRepository.findByExpiryDateBefore(LocalDate.now());
    }

    public List<Medicine> getExpiringSoonMedicines(int days) {
        return medicineRepository.findByExpiryDateBetween(LocalDate.now(), LocalDate.now().plusDays(days));
    }

    public List<Medicine> getMedicinesByCategory(String category) {
        return medicineRepository.findByCategory(category);
    }

    // ── Branch-scoped (SHOPKEEPER) ──────────────────────────────────────────
    public List<Medicine> getMedicinesByBranch(Long branchId) {
        return medicineRepository.findByBranchId(branchId);
    }

    public List<Medicine> searchMedicinesByBranch(Long branchId, String name) {
        return medicineRepository.findByBranchIdAndNameContainingIgnoreCase(branchId, name);
    }

    public List<Medicine> getLowStockByBranch(Long branchId, Integer threshold) {
        return medicineRepository.findByBranchIdAndQuantityLessThan(branchId, threshold);
    }

    public List<Medicine> getExpiredByBranch(Long branchId) {
        return medicineRepository.findByBranchIdAndExpiryDateBefore(branchId, LocalDate.now());
    }

    public List<Medicine> getExpiringSoonByBranch(Long branchId, int days) {
        return medicineRepository.findByBranchIdAndExpiryDateBetween(
                branchId, LocalDate.now(), LocalDate.now().plusDays(days));
    }

    public long countByBranch(Long branchId) {
        return medicineRepository.countByBranchId(branchId);
    }

    public long countLowStockByBranch(Long branchId, Integer threshold) {
        return medicineRepository.countByBranchIdAndQuantityLessThan(branchId, threshold);
    }

    // ── Owner-scoped (OWNER sees all branches they own) ─────────────────────
    public List<Medicine> getMedicinesByOwner(Long ownerId) {
        return medicineRepository.findByOwnerId(ownerId);
    }

    public long countByOwner(Long ownerId) {
        return medicineRepository.countByOwnerId(ownerId);
    }

    public long countLowStockByOwner(Long ownerId, Integer t) {
        return medicineRepository.countLowStockByOwnerId(ownerId, t);
    }

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
