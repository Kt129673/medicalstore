package com.medicalstore.service;

import com.medicalstore.model.Customer;
import com.medicalstore.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerService {

    private final CustomerRepository customerRepository;

    // ── Context-Aware Lookups ───────────────────────────────────────────────
    public List<Customer> getAllCustomers() {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();
        if (tenantId != null)
            return customerRepository.findByBranchId(tenantId);
        if (ownerId != null)
            return customerRepository.findByOwnerId(ownerId);
        return customerRepository.findAll();
    }

    public long countAllCustomers() {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();
        if (tenantId != null)
            return customerRepository.countByBranchId(tenantId);
        if (ownerId != null)
            return customerRepository.countByOwnerId(ownerId);
        return customerRepository.count();
    }

    public Optional<Customer> getCustomerById(Long id) {
        Optional<Customer> customer = customerRepository.findById(id);
        if (customer.isPresent()) {
            Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
            if (tenantId != null && customer.get().getBranch() != null
                && !tenantId.equals(customer.get().getBranch().getId())) {
                // Throw rather than silently return empty — consistent with all other services
                throw new org.springframework.security.access.AccessDeniedException(
                        "Access denied: customer belongs to a different branch");
            }
            Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();
            if (ownerId != null && customer.get().getBranch() != null
                && customer.get().getBranch().getOwner() != null
                && !ownerId.equals(customer.get().getBranch().getOwner().getId())) {
                throw new org.springframework.security.access.AccessDeniedException(
                        "Access denied: customer belongs to a different owner");
            }
        }
        return customer;
    }

    public Optional<Customer> getCustomerByPhone(String ph) {
        // Technically phone uniqueness might be per-branch or global. Assuming global
        // or best-effort context filter.
        Optional<Customer> c = customerRepository.findByPhone(ph);
        if (c.isPresent()) {
            Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
            if (tenantId != null && !tenantId.equals(c.get().getBranch().getId()))
                return Optional.empty();
        }
        return c;
    }

    public List<Customer> searchCustomers(String name) {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();
        if (tenantId != null)
            return customerRepository.findByBranchIdAndNameContainingIgnoreCase(tenantId, name);
        if (ownerId != null)
            return customerRepository.findByOwnerIdAndNameContainingIgnoreCase(ownerId, name);
        return customerRepository.findByNameContainingIgnoreCase(name);
    }

    // ── Legacy Scoped (Kept for explicit calls if needed) ─────────────────────

    // ── Writes ───────────────────────────────────────────────────────────────
    @Transactional
    public Customer saveCustomer(Customer customer) {
        return customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(Long id) {
        customerRepository.deleteById(id);
    }
}
