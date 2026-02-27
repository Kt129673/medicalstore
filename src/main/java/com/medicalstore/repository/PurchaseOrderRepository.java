package com.medicalstore.repository;

import com.medicalstore.model.PurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findAllByOrderByOrderDateDesc();

    Optional<PurchaseOrder> findByOrderNumber(String orderNumber);

    List<PurchaseOrder> findByStatus(String status);

    // --- branch-scoped (SHOPKEEPER) ---
    List<PurchaseOrder> findByBranchIdOrderByOrderDateDesc(Long branchId);

    // --- owner-scoped (OWNER) ---
    @Query("SELECT po FROM PurchaseOrder po WHERE po.branch.owner.id = :ownerId ORDER BY po.orderDate DESC")
    List<PurchaseOrder> findByOwnerId(Long ownerId);

    // --- count queries ---
    long countByStatus(String status);

    @Query("SELECT COALESCE(SUM(po.totalAmount), 0) FROM PurchaseOrder po WHERE po.status = 'RECEIVED'")
    Double totalReceivedAmount();

    @Query("SELECT COALESCE(SUM(po.totalAmount), 0) FROM PurchaseOrder po WHERE po.branch.id = :branchId AND po.status = 'RECEIVED'")
    Double totalReceivedAmountByBranch(Long branchId);
}
