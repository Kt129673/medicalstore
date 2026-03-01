package com.medicalstore.service;

import com.medicalstore.model.PurchaseOrder;
import com.medicalstore.model.PurchaseOrderItem;
import com.medicalstore.repository.MedicineRepository;
import com.medicalstore.repository.PurchaseOrderRepository;
import lombok.RequiredArgsConstructor;
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

    public List<PurchaseOrder> getAllOrders() {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
        if (tenantId != null)
            return purchaseOrderRepository.findByBranchIdOrderByOrderDateDesc(tenantId);
        if (ownerId != null)
            return purchaseOrderRepository.findByOwnerId(ownerId);
        return purchaseOrderRepository.findAllByOrderByOrderDateDesc();
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
            Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
            if (tenantId != null && order.get().getBranch() != null 
                && !tenantId.equals(order.get().getBranch().getId())) {
                return Optional.empty(); // Not authorized
            }
            Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
            if (ownerId != null && order.get().getBranch() != null 
                && order.get().getBranch().getOwner() != null
                && !ownerId.equals(order.get().getBranch().getOwner().getId())) {
                return Optional.empty(); // Not authorized
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
    public PurchaseOrder receiveOrder(Long orderId, List<Integer> receivedQuantities) {
        PurchaseOrder order = purchaseOrderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Purchase order not found"));

        // Tenant check — only the owning branch or admin can receive
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        if (tenantId != null && (order.getBranch() == null || !tenantId.equals(order.getBranch().getId()))) {
            throw new RuntimeException("Access denied: order does not belong to your branch.");
        }
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
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
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        if (tenantId != null && (order.getBranch() == null || !tenantId.equals(order.getBranch().getId()))) {
            throw new RuntimeException("Access denied: order does not belong to your branch.");
        }
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
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
        long count = purchaseOrderRepository.count() + 1;
        return prefix + String.format("%04d", count);
    }
}
