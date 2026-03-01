package com.medicalstore.repository;

import com.medicalstore.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    // --- existing ---
    Optional<Customer> findByEmail(String email);

    Optional<Customer> findByPhone(String phone);

    List<Customer> findByNameContainingIgnoreCase(String name);

    // --- branch-scoped (SHOPKEEPER) ---
    List<Customer> findByBranchId(Long branchId);

    List<Customer> findByBranchIdAndNameContainingIgnoreCase(Long branchId, String name);

    long countByBranchId(Long branchId);

    // --- owner-scoped (OWNER) ---
    @Query("SELECT c FROM Customer c WHERE c.branch.owner.id = :ownerId")
    List<Customer> findByOwnerId(Long ownerId);

    @Query("SELECT COUNT(c) FROM Customer c WHERE c.branch.owner.id = :ownerId")
    long countByOwnerId(Long ownerId);

    // --- owner name search ---
    @Query("SELECT c FROM Customer c WHERE c.branch.owner.id = :ownerId AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    List<Customer> findByOwnerIdAndNameContainingIgnoreCase(Long ownerId, String name);

    // --- Recent Activity ---
    List<Customer> findTop5ByOrderByRegisteredDateDesc();

    List<Customer> findTop5ByBranchIdOrderByRegisteredDateDesc(Long branchId);

    @Query("SELECT c FROM Customer c WHERE c.branch.owner.id = :ownerId ORDER BY c.registeredDate DESC")
    List<Customer> findTop5ByOwnerIdOrderByRegisteredDateDesc(Long ownerId, org.springframework.data.domain.Pageable pageable);
}
