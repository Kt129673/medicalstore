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

    // ── Global (ADMIN) ──────────────────────────────────────────────────────
    public List<Customer> getAllCustomers() {
        return customerRepository.findAll();
    }

    public long countAllCustomers() {
        return customerRepository.count();
    }

    public Optional<Customer> getCustomerById(Long id) {
        return customerRepository.findById(id);
    }

    public Optional<Customer> getCustomerByPhone(String ph) {
        return customerRepository.findByPhone(ph);
    }

    public List<Customer> searchCustomers(String name) {
        return customerRepository.findByNameContainingIgnoreCase(name);
    }

    // ── Branch-scoped (SHOPKEEPER) ──────────────────────────────────────────
    public List<Customer> getCustomersByBranch(Long branchId) {
        return customerRepository.findByBranchId(branchId);
    }

    public long countByBranch(Long branchId) {
        return customerRepository.countByBranchId(branchId);
    }

    public List<Customer> searchCustomersByBranch(Long branchId, String n) {
        return customerRepository.findByBranchIdAndNameContainingIgnoreCase(branchId, n);
    }

    // ── Owner-scoped (OWNER) ────────────────────────────────────────────────
    public List<Customer> getCustomersByOwner(Long ownerId) {
        return customerRepository.findByOwnerId(ownerId);
    }

    public long countByOwner(Long ownerId) {
        return customerRepository.countByOwnerId(ownerId);
    }

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
