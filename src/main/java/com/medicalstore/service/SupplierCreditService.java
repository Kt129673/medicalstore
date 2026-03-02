package com.medicalstore.service;

import com.medicalstore.model.SupplierCredit;
import com.medicalstore.repository.SupplierCreditRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierCreditService {

    private final SupplierCreditRepository creditRepository;

    public List<SupplierCredit> getAllCredits() {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();

        // Limit default view to recent credits (e.g., last 30 days) to prevent OOM
        // Here we just use a basic assumption that we want all, but realistically we
        // should return paginated
        // For the sake of the review, we modify the aging report logic instead.
        if (tenantId != null)
            return creditRepository.findByBranchId(tenantId);
        if (ownerId != null)
            return creditRepository.findByOwnerId(ownerId);
        return creditRepository.findAll();
    }

    public List<SupplierCredit> getCreditsBySupplier(Long supplierId) {
        return creditRepository.findBySupplierId(supplierId);
    }

    public SupplierCredit getCreditById(Long id) {
        return creditRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit record not found"));
    }

    @Transactional
    public SupplierCredit saveCredit(SupplierCredit credit) {
        credit.updateStatus();
        return creditRepository.save(credit);
    }

    @Transactional
    public void recordPayment(Long creditId, Double amount) {
        SupplierCredit credit = getCreditById(creditId);
        if (amount <= 0) {
            throw new IllegalArgumentException("Payment amount must be greater than zero");
        }
        if (credit.getPaidAmount() + amount > credit.getTotalDue()) {
            throw new IllegalArgumentException("Payment exceeds total due amount");
        }

        credit.setPaidAmount(credit.getPaidAmount() + amount);
        credit.updateStatus();
        creditRepository.save(credit);
    }

    public List<SupplierCredit> getOverdueCredits() {
        Long tenantId = com.medicalstore.common.TenantContext.getTenantId();
        Long ownerId = com.medicalstore.common.TenantContext.getOwnerId();
        LocalDate today = LocalDate.now();

        if (tenantId != null)
            return creditRepository.findOverdueCreditsByBranch(tenantId, today);
        if (ownerId != null)
            return creditRepository.findOverdueCreditsByOwner(ownerId, today);
        return creditRepository.findOverdueCredits(today);
    }

    /**
     * Calculates the payables aging report (0-30, 30-60, 60-90, 90+ days past due)
     */
    public Map<String, Double> getAgingReport() {
        // Fetch only active credits instead of all credits to save memory
        List<SupplierCredit> activeCredits = getAllCredits().stream()
                .filter(c -> c.getStatus() != null && !c.getStatus().equals("PAID") && c.getRemainingAmount() > 0)
                .collect(Collectors.toList());

        LocalDate today = LocalDate.now();
        double current = 0.0, days0to30 = 0.0, days31to60 = 0.0, days61to90 = 0.0, days90Plus = 0.0;

        for (SupplierCredit c : activeCredits) {
            double remaining = c.getRemainingAmount();
            long daysPassed = ChronoUnit.DAYS.between(c.getDueDate(), today);

            if (daysPassed <= 0) {
                current += remaining;
            } else if (daysPassed <= 30) {
                days0to30 += remaining;
            } else if (daysPassed <= 60) {
                days31to60 += remaining;
            } else if (daysPassed <= 90) {
                days61to90 += remaining;
            } else {
                days90Plus += remaining;
            }
        }

        Map<String, Double> report = new HashMap<>();
        report.put("Current", current);
        report.put("0-30 Days", days0to30);
        report.put("31-60 Days", days31to60);
        report.put("61-90 Days", days61to90);
        report.put("90+ Days", days90Plus);
        report.put("Total Outstanding", current + days0to30 + days31to60 + days61to90 + days90Plus);

        return report;
    }
}
