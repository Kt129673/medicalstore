package com.medicalstore.service;

import com.medicalstore.common.SecurityUtils;
import com.medicalstore.model.User;
import com.medicalstore.repository.BranchRepository;
import com.medicalstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service to enforce business limits for shopkeepers and validate multi-tenant constraints.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BusinessLimitService {

    private final UserRepository userRepository;
    private final BranchRepository branchRepository;
    private final SecurityUtils securityUtils;
    
    /**
     * Get current username from SecurityContext.
     */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : "unknown";
    }
    
    /**
     * Validate that a shopkeeper cannot create additional branches.
     * Only OWNER and ADMIN can create branches.
     * 
     * @throws SecurityException if validation fails
     */
    public void validateBranchCreation() {
        if (securityUtils.isShopkeeper()) {
            log.error("SECURITY_VIOLATION: Shopkeeper attempted to create branch - User: {}", 
                    getCurrentUsername());
            throw new SecurityException("Shopkeepers are not authorized to create branches");
        }
    }
    
    /**
     * Validate that a user can only access data from their own branch/owner scope.
     * 
     * @param branchId Branch ID being accessed
     * @throws SecurityException if access is denied
     */
    public void validateBranchAccess(Long branchId) {
        if (securityUtils.isAdmin()) {
            // Admin has access to all branches
            return;
        }
        
        if (securityUtils.isOwner()) {
            // Owner can only access their own branches
            Long ownerId = securityUtils.getCurrentUserId();
            boolean ownsThisBranch = branchRepository.findById(branchId)
                    .map(branch -> branch.getOwner().getId().equals(ownerId))
                    .orElse(false);
            
            if (!ownsThisBranch) {
                log.error("SECURITY_VIOLATION: Owner attempted to access another owner's branch - " +
                        "User: {}, BranchId: {}", getCurrentUsername(), branchId);
                throw new SecurityException("Access denied: Branch belongs to another owner");
            }
        } else if (securityUtils.isShopkeeper()) {
            // Shopkeeper can only access their assigned branch
            User currentUser = userRepository.findByUsername(getCurrentUsername())
                    .orElseThrow(() -> new SecurityException("User not found"));
            
            if (currentUser.getBranch() == null || !currentUser.getBranch().getId().equals(branchId)) {
                log.error("SECURITY_VIOLATION: Shopkeeper attempted to access different branch - " +
                        "User: {}, AssignedBranch: {}, RequestedBranch: {}",
                        getCurrentUsername(),
                        currentUser.getBranch() != null ? currentUser.getBranch().getId() : "null",
                        branchId);
                throw new SecurityException("Access denied: You can only access your assigned branch");
            }
        }
    }
    
    /**
     * Validate that shopkeeper cannot bypass tenant isolation using parallel tabs/sessions.
     * This is enforced by TenantContext thread-local which is cleared after each request.
     * 
     * @param expectedBranchId Expected branch ID for current user
     */
    public void validateNoTenantBypass(Long expectedBranchId) {
        Long currentTenantId = com.medicalstore.common.TenantContext.getTenantId();
        
        if (currentTenantId != null && !currentTenantId.equals(expectedBranchId)) {
            log.error("SECURITY_VIOLATION: Tenant context mismatch - Expected: {}, Found: {}", 
                    expectedBranchId, currentTenantId);
            throw new SecurityException("Tenant context violation detected");
        }
    }
    
    /**
     * Validate that a sale does not result in negative stock.
     * This must be called within a transaction before stock deduction.
     * 
     * @param medicineId Medicine ID
     * @param requestedQuantity Quantity requested
     * @param availableStock Current stock level
     * @throws IllegalStateException if stock would go negative
     */
    public void validateStockAvailability(Long medicineId, int requestedQuantity, int availableStock) {
        if (requestedQuantity > availableStock) {
            log.error("BUSINESS_RULE_VIOLATION: Attempted sale with insufficient stock - " +
                    "MedicineId: {}, Requested: {}, Available: {}, User: {}",
                    medicineId, requestedQuantity, availableStock, getCurrentUsername());
            throw new IllegalStateException(
                    String.format("Insufficient stock: Requested %d, Available %d", 
                            requestedQuantity, availableStock));
        }
        
        if (requestedQuantity <= 0) {
            log.error("BUSINESS_RULE_VIOLATION: Attempted sale with invalid quantity - " +
                    "MedicineId: {}, Quantity: {}, User: {}",
                    medicineId, requestedQuantity, getCurrentUsername());
            throw new IllegalArgumentException("Sale quantity must be positive");
        }
    }
    
    /**
     * Validate concurrent sale scenario using optimistic locking.
     * The actual validation happens at DB level through @Version field in Medicine entity.
     * This method logs the detection for monitoring.
     */
    public void logConcurrentSaleAttempt(Long medicineId, String scenario) {
        log.warn("CONCURRENT_SALE_DETECTED: MedicineId: {}, Scenario: {}, User: {}",
                medicineId, scenario, getCurrentUsername());
    }
    
    /**
     * Validate that shopkeeper cannot modify data from other branches.
     * 
     * @param resourceBranchId Branch ID of the resource being modified
     * @throws SecurityException if validation fails
     */
    public void validateDataOwnership(Long resourceBranchId) {
        if (securityUtils.isAdmin()) {
            return; // Admin can modify all data
        }
        
        if (securityUtils.isShopkeeper()) {
            // Shopkeeper can only modify data from their branch
            validateBranchAccess(resourceBranchId);
        } else if (securityUtils.isOwner()) {
            // Owner can modify data from their branches
            validateBranchAccess(resourceBranchId);
        }
    }
}
