package com.medicalstore.service;

import com.medicalstore.model.Branch;
import com.medicalstore.dto.BranchComparisonDTO;
import com.medicalstore.repository.BranchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    /**
     * Get optimized branch comparison data for an owner.
     * This method uses batch queries to avoid N+1 problem.
     * 
     * @param ownerId Owner ID
     * @return List of BranchComparisonDTO with all metrics populated
     */
    @Cacheable(value = "branch_comparison", key = "#ownerId")
    public List<BranchComparisonDTO> getBranchComparisonData(Long ownerId) {
        // Step 1: Get base branch info
        List<BranchComparisonDTO> branches = branchRepository.getBranchComparisonBase(ownerId);
        
        if (branches.isEmpty()) {
            return branches;
        }
        
        // Step 2: Build lookup maps for efficient population
        Map<Long, BranchComparisonDTO> branchMap = new HashMap<>();
        for (BranchComparisonDTO dto : branches) {
            branchMap.put(dto.getBranchId(), dto);
        }
        
        // Step 3: Populate medicine counts
        List<Object[]> medicineCounts = branchRepository.getMedicineCountsByOwner(ownerId);
        for (Object[] row : medicineCounts) {
            Long branchId = (Long) row[0];
            Long count = (Long) row[1];
            if (branchMap.containsKey(branchId)) {
                branchMap.get(branchId).setTotalMedicines(count);
            }
        }
        
        // Step 4: Populate low stock counts
        List<Object[]> lowStockCounts = branchRepository.getLowStockCountsByOwner(ownerId);
        for (Object[] row : lowStockCounts) {
            Long branchId = (Long) row[0];
            Long count = (Long) row[1];
            if (branchMap.containsKey(branchId)) {
                branchMap.get(branchId).setLowStockCount(count);
            }
        }
        
        // Step 5: Populate expiring counts (30 days)
        LocalDate now = LocalDate.now();
        LocalDate thirtyDaysLater = now.plusDays(30);
        List<Object[]> expiringCounts = branchRepository.getExpiringCountsByOwner(ownerId, now, thirtyDaysLater);
        for (Object[] row : expiringCounts) {
            Long branchId = (Long) row[0];
            Long count = (Long) row[1];
            if (branchMap.containsKey(branchId)) {
                branchMap.get(branchId).setExpiringCount(count);
            }
        }
        
        // Step 6: Populate shopkeeper counts
        List<Object[]> shopkeeperCounts = branchRepository.getShopkeeperCountsByOwner(ownerId);
        for (Object[] row : shopkeeperCounts) {
            Long branchId = (Long) row[0];
            Long count = (Long) row[1];
            if (branchMap.containsKey(branchId)) {
                branchMap.get(branchId).setTotalShopkeepers(count);
            }
        }
        
        // Step 7: Populate active shopkeeper counts
        List<Object[]> activeShopkeeperCounts = branchRepository.getActiveShopkeeperCountsByOwner(ownerId);
        for (Object[] row : activeShopkeeperCounts) {
            Long branchId = (Long) row[0];
            Long count = (Long) row[1];
            if (branchMap.containsKey(branchId)) {
                branchMap.get(branchId).setActiveShopkeepers(count);
            }
        }
        
        // Step 8: Populate customer counts
        List<Object[]> customerCounts = branchRepository.getCustomerCountsByOwner(ownerId);
        for (Object[] row : customerCounts) {
            Long branchId = (Long) row[0];
            Long count = (Long) row[1];
            if (branchMap.containsKey(branchId)) {
                branchMap.get(branchId).setTotalCustomers(count);
            }
        }
        
        // Step 9: Populate today's sales
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        List<Object[]> todaySales = branchRepository.getSalesTotalsByOwner(ownerId, startOfDay, endOfDay);
        for (Object[] row : todaySales) {
            Long branchId = (Long) row[0];
            Double amount = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            if (branchMap.containsKey(branchId)) {
                branchMap.get(branchId).setTodaySales(amount);
            }
        }
        
        // Step 10: Populate monthly revenue
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        List<Object[]> monthlyRevenue = branchRepository.getSalesTotalsByOwner(ownerId, startOfMonth, endOfDay);
        for (Object[] row : monthlyRevenue) {
            Long branchId = (Long) row[0];
            Double amount = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
            if (branchMap.containsKey(branchId)) {
                branchMap.get(branchId).setMonthlyRevenue(amount);
            }
        }
        
        return branches;
    }
}

