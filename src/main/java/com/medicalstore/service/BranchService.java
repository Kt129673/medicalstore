package com.medicalstore.service;

import com.medicalstore.model.Branch;
import com.medicalstore.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BranchService {

    private final BranchRepository branchRepository;

    public List<Branch> getAllBranches() {
        return branchRepository.findAll();
    }

    public List<Branch> getActiveBranches() {
        return branchRepository.findByIsActiveTrue();
    }

    public List<Branch> getBranchesByOwner(Long ownerId) {
        return branchRepository.findByOwnerId(ownerId);
    }

    public List<Branch> getActiveBranchesByOwner(Long ownerId) {
        return branchRepository.findByOwnerIdAndIsActiveTrue(ownerId);
    }

    public Optional<Branch> getBranchById(Long id) {
        return branchRepository.findById(id);
    }

    public long countByOwner(Long ownerId) {
        return branchRepository.countByOwnerId(ownerId);
    }

    @Transactional
    public Branch saveBranch(Branch branch) {
        return branchRepository.save(branch);
    }

    @Transactional
    public void toggleActive(Long id) {
        branchRepository.findById(id).ifPresent(b -> {
            b.setIsActive(!b.getIsActive());
            branchRepository.save(b);
        });
    }

    @Transactional
    public void deleteBranch(Long id) {
        branchRepository.deleteById(id);
    }
}
