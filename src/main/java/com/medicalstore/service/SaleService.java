package com.medicalstore.service;

import com.medicalstore.model.Sale;
import com.medicalstore.model.Medicine;
import com.medicalstore.model.Customer;
import com.medicalstore.repository.SaleRepository;
import com.medicalstore.repository.MedicineRepository;
import com.medicalstore.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SaleService {

    private final SaleRepository saleRepository;
    private final MedicineRepository medicineRepository;
    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public Optional<Sale> getSaleById(Long id) {
        // Single JOIN FETCH query — avoids N+1 on invoice/detail view
        Optional<Sale> sale = saleRepository.findByIdWithDetails(id);
        if (sale.isPresent() && sale.get().getBranch() != null) {
            Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
            if (tenantId != null && !tenantId.equals(sale.get().getBranch().getId())) {
                return Optional.empty();
            }
            Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();
            if (ownerId != null && sale.get().getBranch().getOwner() != null
                    && !ownerId.equals(sale.get().getBranch().getOwner().getId())) {
                return Optional.empty();
            }
        }
        return sale;
    }

    @Transactional(readOnly = true)
    public List<Sale> getAllSales() {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        if (tenantId != null)
            return saleRepository.findByBranchId(tenantId);
        if (ownerId != null)
            return saleRepository.findByOwnerId(ownerId);
        return saleRepository.findAll();
    }

    /**
     * Returns sales with items and medicines eagerly loaded.
     * Used by the Return form dropdown to avoid LazyInitializationException.
     */
    @Transactional(readOnly = true)
    public List<Sale> getAllSalesWithItems() {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        if (tenantId != null)
            return saleRepository.findByBranchIdWithItems(tenantId);
        if (ownerId != null)
            return saleRepository.findByOwnerIdWithItems(ownerId);
        return saleRepository.findAllWithItems();
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Sale> getSalesPaginated(
            org.springframework.data.domain.Pageable pageable) {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        if (tenantId != null)
            return saleRepository.findByBranchIdOrderBySaleDateDesc(tenantId, pageable);
        if (ownerId != null)
            return saleRepository.findByBranchOwnerIdOrderBySaleDateDesc(ownerId, pageable);
        return saleRepository.findAllByOrderBySaleDateDesc(pageable);
    }

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<Sale> getFilteredSalesPaginated(
            String search, LocalDateTime startDate, LocalDateTime endDate,
            String paymentMethod, org.springframework.data.domain.Pageable pageable) {

        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        org.springframework.data.jpa.domain.Specification<Sale> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();

            // Tenant isolation
            if (tenantId != null) {
                predicates.add(cb.equal(root.get("branch").get("id"), tenantId));
            } else if (ownerId != null) {
                predicates.add(cb.equal(root.get("branch").get("owner").get("id"), ownerId));
            }

            // Customer name search — only join when actually filtering
            if (search != null && !search.trim().isEmpty()) {
                String like = "%" + search.toLowerCase() + "%";
                jakarta.persistence.criteria.Join<?, ?> customerJoin = root.join("customer",
                        jakarta.persistence.criteria.JoinType.LEFT);
                predicates.add(cb.like(cb.lower(customerJoin.get("name")), like));
                query.distinct(true);
            }

            // Date range
            if (startDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("saleDate"), startDate));
            }
            if (endDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("saleDate"), endDate));
            }

            // Payment method
            if (paymentMethod != null && !paymentMethod.trim().isEmpty()) {
                predicates.add(cb.equal(cb.lower(root.get("paymentMethod")), paymentMethod.toLowerCase()));
            }

            // Only add ORDER BY for the select query (not the count query)
            if (query.getResultType() != Long.class && query.getResultType() != long.class) {
                query.orderBy(cb.desc(root.get("saleDate")));
            }

            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        return saleRepository.findAll(spec, pageable);
    }

    @Transactional
    public Sale createSale(Sale sale) {
        if (sale.getItems() == null || sale.getItems().isEmpty()) {
            throw new RuntimeException("Sale must have at least one item.");
        }

        // Batch-load all medicines in ONE query instead of N individual findById calls
        List<Long> medicineIds = sale.getItems().stream()
                .map(item -> item.getMedicine().getId())
                .collect(java.util.stream.Collectors.toList());
        java.util.Map<Long, Medicine> medicineMap = medicineRepository.findAllById(medicineIds).stream()
                .collect(java.util.stream.Collectors.toMap(Medicine::getId, m -> m));

        double calculatedTotalAmt = 0.0;

        for (com.medicalstore.model.SaleItem item : sale.getItems()) {
            Medicine medicine = medicineMap.get(item.getMedicine().getId());
            if (medicine == null) {
                throw new RuntimeException("Medicine not found: id=" + item.getMedicine().getId());
            }

            if (medicine.getQuantity() < item.getQuantity()) {
                throw new RuntimeException("Insufficient stock for medicine: " + medicine.getName());
            }

            // Deduct stock atomically
            int updatedRows = medicineRepository.deductStock(medicine.getId(), item.getQuantity());
            if (updatedRows == 0) {
                throw new RuntimeException(
                        "Insufficient stock for medicine: " + medicine.getName() + " or it does not exist.");
            }
            medicine.setQuantity(medicine.getQuantity() - item.getQuantity());

            // Re-assign accurate entity
            item.setMedicine(medicine);
            item.setCostPrice(medicine.getPurchasePrice() != null ? medicine.getPurchasePrice() : 0.0);
            item.calculateTotal();
            calculatedTotalAmt += item.getTotalPrice();
        }

        sale.setTotalAmount(calculatedTotalAmt);

        // Auto-assign branch if tenant is set (Shopkeeper)
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        if (tenantId != null && sale.getBranch() == null) {
            com.medicalstore.model.Branch b = new com.medicalstore.model.Branch();
            b.setId(tenantId);
            sale.setBranch(b);
        }

        // Award loyalty points (1 point for every ₹100 spent)
        if (sale.getCustomer() != null && sale.getCustomer().getId() != null) {
            Customer customer = customerRepository.findById(sale.getCustomer().getId()).orElse(null);
            if (customer != null) {
                // To accurately reward points, we calculate discount and final amount first
                double discountAmt = 0.0;
                if (sale.getDiscountPercentage() != null && sale.getDiscountPercentage() > 0) {
                    discountAmt = (calculatedTotalAmt * sale.getDiscountPercentage()) / 100;
                }
                sale.setDiscountAmount(discountAmt);
                double amtAfterDiscount = calculatedTotalAmt - discountAmt;

                double gstAmt = 0.0;
                if (sale.getGstPercentage() != null && sale.getGstPercentage() > 0) {
                    gstAmt = (amtAfterDiscount * sale.getGstPercentage()) / 100;
                }
                sale.setGstAmount(gstAmt);
                sale.setFinalAmount(amtAfterDiscount + gstAmt);

                int pointsToAdd = (int) (sale.getFinalAmount() / 100);
                customer.setLoyaltyPoints(customer.getLoyaltyPoints() + pointsToAdd);
                customerRepository.save(customer);
                sale.setCustomer(customer);
            }
        }

        return saleRepository.save(sale);
    }

    @Transactional
    public void deleteSale(Long id) {
        Sale sale = saleRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new RuntimeException("Sale not found: " + id));

        // Restore stock for each item before deleting
        for (com.medicalstore.model.SaleItem item : sale.getItems()) {
            medicineRepository.findById(item.getMedicine().getId()).ifPresent(medicine -> {
                medicine.setQuantity(medicine.getQuantity() + item.getQuantity());
                medicineRepository.save(medicine);
            });
        }

        saleRepository.delete(sale);
    }

    public List<Sale> getSalesByCustomer(Long customerId) {
        // Assuming customer data is global or handled by branch. If strictly isolated,
        // filter it.
        return saleRepository.findByCustomerId(customerId);
    }

    public List<Sale> getSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        if (tenantId != null)
            return saleRepository.findByBranchIdAndSaleDateBetween(tenantId, start, end);
        if (ownerId != null)
            return saleRepository.findByOwnerIdBetween(ownerId, start, end);
        return saleRepository.findBySaleDateBetween(start, end);
    }

    public Double getTodaySales() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        Double total;
        if (tenantId != null)
            total = saleRepository.getTotalSalesByBranchBetween(tenantId, startOfDay, endOfDay);
        else if (ownerId != null)
            total = saleRepository.getTotalSalesByOwnerBetween(ownerId, startOfDay, endOfDay);
        else
            total = saleRepository.getTotalSalesBetween(startOfDay, endOfDay);

        return total != null ? total : 0.0;
    }

    public Double getTotalSalesBetween(LocalDateTime start, LocalDateTime end) {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        Double total;
        if (tenantId != null)
            total = saleRepository.getTotalSalesByBranchBetween(tenantId, start, end);
        else if (ownerId != null)
            total = saleRepository.getTotalSalesByOwnerBetween(ownerId, start, end);
        else
            total = saleRepository.getTotalSalesBetween(start, end);

        return total != null ? total : 0.0;
    }

    public List<Sale> getRecentSales() {
        Long tenantId = com.medicalstore.config.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.config.TenantContext.getOwnerId();

        if (tenantId != null)
            return saleRepository.findTop5ByBranchIdOrderBySaleDateDesc(tenantId);
        if (ownerId != null)
            return saleRepository.findTop5ByOwnerIdOrderBySaleDateDesc(ownerId);
        return saleRepository.findTop5ByOrderBySaleDateDesc();
    }
}
