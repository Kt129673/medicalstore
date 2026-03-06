package com.medicalstore.service;

import com.medicalstore.common.TenantContext;
import com.medicalstore.model.Supplier;
import com.medicalstore.repository.SupplierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierService {

    private final SupplierRepository supplierRepository;
    private final RoleAuditService roleAuditService;

    public List<Supplier> getAllSuppliers() {
        Long tenantId = TenantContext.getTenantId();
        Long ownerId = TenantContext.getOwnerId();
        if (tenantId != null)
            return supplierRepository.findByBranchId(tenantId);
        if (ownerId != null)
            return supplierRepository.findByOwnerId(ownerId);
        return supplierRepository.findAll();
    }

    public Optional<Supplier> getSupplierById(Long id) {
        Optional<Supplier> supplier = supplierRepository.findById(id);
        if (supplier.isPresent()) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId != null && supplier.get().getBranch() != null
                    && !tenantId.equals(supplier.get().getBranch().getId())) {
                roleAuditService.logEscalationAttempt("/suppliers/" + id, "SHOPKEEPER",
                        "Attempted to access supplier from different branch (branchId="
                                + supplier.get().getBranch().getId() + ")");
                throw new AccessDeniedException("Access denied: supplier belongs to a different branch");
            }
            Long ownerId = TenantContext.getOwnerId();
            if (ownerId != null && supplier.get().getBranch() != null
                    && supplier.get().getBranch().getOwner() != null
                    && !ownerId.equals(supplier.get().getBranch().getOwner().getId())) {
                roleAuditService.logEscalationAttempt("/suppliers/" + id, "OWNER",
                        "Attempted to access supplier belonging to different owner");
                throw new AccessDeniedException("Access denied: supplier belongs to a different owner");
            }
        }
        return supplier;
    }

    @Transactional
    public Supplier saveSupplier(Supplier supplier) {
        if (supplier.getName() == null || supplier.getName().isBlank())
            throw new IllegalArgumentException("Supplier name is required");
        if (supplier.getContactPerson() == null || supplier.getContactPerson().isBlank())
            throw new IllegalArgumentException("Supplier contact person is required");
        if (supplier.getPhone() == null || supplier.getPhone().isBlank())
            throw new IllegalArgumentException("Supplier phone is required");
        if (supplier.getAddress() == null || supplier.getAddress().isBlank())
            throw new IllegalArgumentException("Supplier address is required");
        return supplierRepository.save(supplier);
    }

    @Transactional
    public void deleteSupplier(Long id) {
        supplierRepository.deleteById(id);
    }

    public List<Supplier> searchSuppliers(String name) {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();
        if (tenantId != null)
            return supplierRepository.findByBranchIdAndNameContainingIgnoreCase(tenantId, name);
        if (ownerId != null)
            return supplierRepository.findByOwnerIdAndNameContainingIgnoreCase(ownerId, name);
        return supplierRepository.findByNameContainingIgnoreCase(name);
    }
}
