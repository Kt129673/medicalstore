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

        // Award loyalty points (1 point for every ₹100 spent)
        if (sale.getCustomer() != null) {
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
        return saleRepository.findByCustomerId(customerId);
    }

    public List<Sale> getSalesByDateRange(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetween(start, end);
    }

    public Double getTodaySales() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        Double total = saleRepository.getTotalSalesBetween(startOfDay, endOfDay);
        return total != null ? total : 0.0;
    }

    public Double getTotalSalesBetween(LocalDateTime start, LocalDateTime end) {
        Double total = saleRepository.getTotalSalesBetween(start, end);
        return total != null ? total : 0.0;
    }

    public List<Sale> getRecentSales() {
        return saleRepository.findTop5ByOrderBySaleDateDesc();
    }

    // ── Branch-scoped (SHOPKEEPER) ──────────────────────────────────────────
    public List<Sale> getAllSalesByBranch(Long branchId) {
        return saleRepository.findByBranchId(branchId);
    }

    public List<Sale> getRecentSalesByBranch(Long branchId) {
        return saleRepository.findTop5ByBranchIdOrderBySaleDateDesc(branchId);
    }

    public Double getTodaySalesByBranch(Long branchId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        Double total = saleRepository.getTotalSalesByBranchBetween(branchId, start, end);
        return total != null ? total : 0.0;
    }

    // ── Owner-scoped (OWNER) ────────────────────────────────────────────────
    public List<Sale> getAllSalesByOwner(Long ownerId) {
        return saleRepository.findByOwnerId(ownerId);
    }

    public List<Sale> getRecentSalesByOwner(Long ownerId) {
        return saleRepository.findTop5ByOwnerIdOrderBySaleDateDesc(ownerId);
    }

    public Double getTodaySalesByOwner(Long ownerId) {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDate.now().atTime(23, 59, 59);
        Double total = saleRepository.getTotalSalesByOwnerBetween(ownerId, start, end);
        return total != null ? total : 0.0;
    }
}
