package com.medicalstore.repository;

import com.medicalstore.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    List<Branch> findByOwnerId(Long ownerId);

    List<Branch> findByOwnerIdAndIsActiveTrue(Long ownerId);

    List<Branch> findByIsActiveTrue();

    long countByOwnerId(Long ownerId);
}
