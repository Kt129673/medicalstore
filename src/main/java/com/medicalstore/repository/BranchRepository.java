package com.medicalstore.repository;

import com.medicalstore.model.Branch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BranchRepository extends JpaRepository<Branch, Long> {

    @Query("SELECT b FROM Branch b JOIN FETCH b.owner WHERE b.owner.id = :ownerId")
    List<Branch> findByOwnerId(Long ownerId);

    @Query("SELECT b FROM Branch b JOIN FETCH b.owner WHERE b.owner.id = :ownerId AND b.isActive = true")
    List<Branch> findByOwnerIdAndIsActiveTrue(Long ownerId);

    @Query("SELECT b FROM Branch b JOIN FETCH b.owner WHERE b.isActive = true")
    List<Branch> findByIsActiveTrue();

    @Query("SELECT b FROM Branch b JOIN FETCH b.owner")
    List<Branch> findAll();

    long countByOwnerId(Long ownerId);
}
