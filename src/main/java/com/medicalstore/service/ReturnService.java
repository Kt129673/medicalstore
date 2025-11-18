package com.medicalstore.service;

import com.medicalstore.model.Return;
import com.medicalstore.model.Sale;
import com.medicalstore.model.Medicine;
import com.medicalstore.repository.ReturnRepository;
import com.medicalstore.repository.SaleRepository;
import com.medicalstore.repository.MedicineRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReturnService {
    
    private final ReturnRepository returnRepository;
    private final SaleRepository saleRepository;
    private final MedicineRepository medicineRepository;
    
    public List<Return> getAllReturns() {
        return returnRepository.findAll();
    }
    
    public Optional<Return> getReturnById(Long id) {
        return returnRepository.findById(id);
    }
    
    @Transactional
    public Return createReturn(Return returnItem) {
        // Validate return quantity
        Sale sale = returnItem.getSale();
        if (returnItem.getReturnQuantity() > sale.getQuantity()) {
            throw new RuntimeException("Return quantity cannot exceed sold quantity");
        }
        
        // Restore medicine stock
        Medicine medicine = sale.getMedicine();
        medicine.setQuantity(medicine.getQuantity() + returnItem.getReturnQuantity());
        medicineRepository.save(medicine);
        
        return returnRepository.save(returnItem);
    }
    
    public List<Return> getReturnsBySale(Long saleId) {
        return returnRepository.findBySaleId(saleId);
    }
}
