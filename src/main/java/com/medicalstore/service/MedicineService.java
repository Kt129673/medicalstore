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

    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }

    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }

    public Medicine saveMedicine(Medicine medicine) {
        // Validate required fields
        if (medicine.getName() == null || medicine.getName().trim().isEmpty()) {
            throw new IllegalArgumentException("Medicine name is required");
        }
        if (medicine.getCategory() == null || medicine.getCategory().trim().isEmpty()) {
            throw new IllegalArgumentException("Category is required");
        }
        if (medicine.getPrice() == null || medicine.getPrice() <= 0) {
            throw new IllegalArgumentException("Valid price is required");
        }
        if (medicine.getQuantity() == null || medicine.getQuantity() < 0) {
            throw new IllegalArgumentException("Valid quantity is required");
        }

        // Set created date if new medicine
        if (medicine.getId() == null && medicine.getCreatedDate() == null) {
            medicine.setCreatedDate(LocalDate.now());
        }

        return medicineRepository.save(medicine);
    }

    public void deleteMedicine(Long id) {
        medicineRepository.deleteById(id);
    }

    public List<Medicine> searchMedicines(String name) {
        return medicineRepository.findByNameContainingIgnoreCase(name);
    }

    public List<Medicine> getLowStockMedicines(Integer threshold) {
        return medicineRepository.findByQuantityLessThan(threshold);
    }

    /** COUNT only — avoids loading all rows just to get the count */
    public long countLowStockMedicines(Integer threshold) {
        return medicineRepository.countByQuantityLessThan(threshold);
    }

    /** Total medicine count via COUNT(*) */
    public long countAllMedicines() {
        return medicineRepository.count();
    }

    public List<Medicine> getExpiredMedicines() {
        return medicineRepository.findByExpiryDateBefore(LocalDate.now());
    }

    public List<Medicine> getExpiringSoonMedicines(int days) {
        LocalDate today = LocalDate.now();
        LocalDate futureDate = today.plusDays(days);
        return medicineRepository.findByExpiryDateBetween(today, futureDate);
    }

    public List<Medicine> getMedicinesByCategory(String category) {
        return medicineRepository.findByCategory(category);
    }
}
