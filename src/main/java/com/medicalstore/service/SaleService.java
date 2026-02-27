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
        return saleRepository.findByIdWithDetails(id);
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

    @Transactional
    public Sale createSale(Sale sale) {
        // Fetch the full medicine object from database
        Medicine medicine = medicineRepository.findById(sale.getMedicine().getId())
                .orElseThrow(() -> new RuntimeException("Medicine not found"));

        if (medicine.getQuantity() < sale.getQuantity()) {
            throw new RuntimeException("Insufficient stock for medicine: " + medicine.getName());
        }

        medicine.setQuantity(medicine.getQuantity() - sale.getQuantity());
        medicineRepository.save(medicine);

        // Set the full medicine object to sale
        sale.setMedicine(medicine);

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
                Double finalAmount = sale.getFinalAmount() != null ? sale.getFinalAmount() : sale.getTotalAmount();
                int pointsToAdd = (int) (finalAmount / 100);
                customer.setLoyaltyPoints(customer.getLoyaltyPoints() + pointsToAdd);
                customerRepository.save(customer);
                sale.setCustomer(customer);
            }
        }

        return saleRepository.save(sale);
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
