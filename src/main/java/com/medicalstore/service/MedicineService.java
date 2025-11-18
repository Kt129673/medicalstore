package com.medicalstore.service;

import com.medicalstore.model.Medicine;
import com.medicalstore.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MedicineService {
    
    private final MedicineRepository medicineRepository;
    
    public List<Medicine> getAllMedicines() {
        return medicineRepository.findAll();
    }
    
    public Optional<Medicine> getMedicineById(Long id) {
        return medicineRepository.findById(id);
    }
    
    public Medicine saveMedicine(Medicine medicine) {
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
