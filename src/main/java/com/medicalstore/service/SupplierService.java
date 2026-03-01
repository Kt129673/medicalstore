package com.medicalstore.service;

import com.medicalstore.model.Supplier;
import com.medicalstore.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SupplierService {
    
    private final SupplierRepository supplierRepository;
    
    public List<Supplier> getAllSuppliers() {
        return supplierRepository.findAll();
    }
    
    public Optional<Supplier> getSupplierById(Long id) {
        return supplierRepository.findById(id);
    }
    
    @Transactional
    public Supplier saveSupplier(Supplier supplier) {
        return supplierRepository.save(supplier);
    }
    
    @Transactional
    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }
    
    public List<Supplier> searchSuppliers(String name) {
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }
}
