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
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
        if (tenantId != null)
            return supplierRepository.findByBranchId(tenantId);
        if (ownerId != null)
            return supplierRepository.findByOwnerId(ownerId);
        return supplierRepository.findAll();
    }
    
    public Optional<Supplier> getSupplierById(Long id) {
        Optional<Supplier> supplier = supplierRepository.findById(id);
        if (supplier.isPresent()) {
            Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
            if (tenantId != null && supplier.get().getBranch() != null
                    && !tenantId.equals(supplier.get().getBranch().getId())) {
                return Optional.empty();
            }
            Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
            if (ownerId != null && supplier.get().getBranch() != null
                    && supplier.get().getBranch().getOwner() != null
                    && !ownerId.equals(supplier.get().getBranch().getOwner().getId())) {
                return Optional.empty();
            }
        }
        return supplier;
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
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
        if (tenantId != null)
            return supplierRepository.findByBranchIdAndNameContainingIgnoreCase(tenantId, name);
        if (ownerId != null)
            return supplierRepository.findByOwnerIdAndNameContainingIgnoreCase(ownerId, name);
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }
}
