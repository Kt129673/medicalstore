package com.medicalstore.service;

import com.medicalstore.common.TenantContext;
import com.medicalstore.model.PurchaseOrder;
import com.medicalstore.model.PurchaseOrderItem;
import com.medicalstore.repository.MedicineRepository;
import com.medicalstore.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PurchaseService {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final MedicineRepository medicineRepository;
    private final RoleAuditService roleAuditService;

    public List<PurchaseOrder> getAllOrders() {
        Long tenantId = TenantContext.getTenantId();
        Long ownerId = TenantContext.getOwnerId();
        if (tenantId != null)
            return purchaseOrderRepository.findByBranchIdOrderByOrderDateDesc(tenantId);
        if (ownerId != null)
            return purchaseOrderRepository.findByOwnerId(ownerId);
        return purchaseOrderRepository.findAllByOrderByOrderDateDesc();
    }

    /**
     * Paginated version of {@link #getAllOrders()}.
     * Returns a {@link Page} of orders scoped to the current tenant/owner.
     */
    public Page<PurchaseOrder> getAllOrdersPaged(Pageable pageable) {
        Long tenantId = TenantContext.getTenantId();
        Long ownerId = TenantContext.getOwnerId();
        if (tenantId != null)
            return purchaseOrderRepository.findByBranchIdOrderByOrderDateDesc(tenantId, pageable);
        if (ownerId != null)
            return purchaseOrderRepository.findByOwnerIdPageable(ownerId, pageable);
        return purchaseOrderRepository.findAllByOrderByOrderDateDesc(pageable);
    }

    public List<PurchaseOrder> getOrdersByBranch(Long branchId) {
        return purchaseOrderRepository.findByBranchIdOrderByOrderDateDesc(branchId);
    }

    public List<PurchaseOrder> getOrdersByOwner(Long ownerId) {
        return purchaseOrderRepository.findByOwnerId(ownerId);
    }

    public Optional<PurchaseOrder> getOrderById(Long id) {
        Optional<PurchaseOrder> order = purchaseOrderRepository.findById(id);
        if (order.isPresent()) {
            Long tenantId = TenantContext.getTenantId();
            if (tenantId != null && order.get().getBranch() != null
                    && !tenantId.equals(order.get().getBranch().getId())) {
                roleAuditService.logEscalationAttempt("/purchases/" + id, "SHOPKEEPER",
                        "Attempted to access purchase order from different branch (branchId="
                                + order.get().getBranch().getId() + ")");
                throw new AccessDeniedException("Access denied: purchase order belongs to a different branch");
            }
            Long ownerId = TenantContext.getOwnerId();
            if (ownerId != null && order.get().getBranch() != null
                    && order.get().getBranch().getOwner() != null
                    && !ownerId.equals(order.get().getBranch().getOwner().getId())) {
                roleAuditService.logEscalationAttempt("/purchases/" + id, "OWNER",
                        "Attempted to access purchase order belonging to different owner");
                throw new AccessDeniedException("Access denied: purchase order belongs to a different owner");
            }
        }
        return order;
    }

    @Transactional
    public PurchaseOrder saveOrder(PurchaseOrder order) {
        if (order.getOrderNumber() == null || order.getOrderNumber().isBlank()) {
            order.setOrderNumber(generateOrderNumber());
        }
        // recalculate item totals and order total
        for (PurchaseOrderItem item : order.getItems()) {
            item.setPurchaseOrder(order);
            if (item.getQuantity() != null && item.getUnitPrice() != null) {
                item.setTotalPrice(item.getQuantity() * item.getUnitPrice());
            }
        }
        order.recalculateTotal();
        return purchaseOrderRepository.save(order);
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "medicines_search", allEntries = true),
            @CacheEvict(value = "dashboard_kpis", allEntries = true)
    })
    public PurchaseOrder receiveOrder(Long orderId, List<Integer> receivedQuantities) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        // Tenant check — only the owning branch or admin can receive
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        if (tenantId != null && (order.getBranch() == null || !tenantId.equals(order.getBranch().getId()))) {
            throw new RuntimeException("Access denied: order does not belong to your branch.");
        }
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();
        if (ownerId != null && order.getBranch() != null && order.getBranch().getOwner() != null
                && !ownerId.equals(order.getBranch().getOwner().getId())) {
            throw new RuntimeException("Access denied: order does not belong to your organisation.");
        }

        List<PurchaseOrderItem> items = order.getItems();
        for (int i = 0; i < items.size() && i < receivedQuantities.size(); i++) {
            PurchaseOrderItem item = items.get(i);
            int received = receivedQuantities.get(i);
            item.setReceivedQuantity(received);

            // Update medicine stock atomically
            medicineRepository.addStock(item.getMedicine().getId(), received);
            // Optional: Update the in-memory object if needed
            item.getMedicine().setQuantity(item.getMedicine().getQuantity() + received);
        }

        order.setStatus("RECEIVED");
        order.setReceivedDate(LocalDate.now());
        return purchaseOrderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        // Tenant check
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        if (tenantId != null && (order.getBranch() == null || !tenantId.equals(order.getBranch().getId()))) {
            throw new RuntimeException("Access denied: order does not belong to your branch.");
        }
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();
        if (ownerId != null && order.getBranch() != null && order.getBranch().getOwner() != null
                && !ownerId.equals(order.getBranch().getOwner().getId())) {
            throw new RuntimeException("Access denied: order does not belong to your organisation.");
        }

        order.setStatus("CANCELLED");
        purchaseOrderRepository.save(order);
    }

    @Transactional
    public void deleteOrder(Long id) {
        purchaseOrderRepository.deleteById(id);
    }

    private String generateOrderNumber() {
        String prefix = "PO-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + "-";
        String uniqueSuffix = java.util.UUID.randomUUID().toString().substring(0, 6).toUpperCase(java.util.Locale.ROOT);
        return prefix + uniqueSuffix;
    }
}
