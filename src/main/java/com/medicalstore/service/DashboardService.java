package com.medicalstore.service;

import com.medicalstore.model.Medicine;
import com.medicalstore.model.Sale;
import com.medicalstore.repository.MedicineRepository;
import com.medicalstore.repository.SaleRepository;
import com.medicalstore.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final SaleRepository saleRepository;
    private final MedicineRepository medicineRepository;
    private final CustomerRepository customerRepository;

    // ═══════════════════════════════════════════════════════════════════
    // Build dashboard data map for a given scope
    // ═══════════════════════════════════════════════════════════════════

    /** Global dashboard (ADMIN) */
    @Cacheable("dashboard_kpis")
    public Map<String, Object> buildAdminDashboard() {
        Map<String, Object> data = new LinkedHashMap<>();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();

        // KPI cards
        Double todaySales = saleRepository.getRevenueBetween(startOfDay, endOfDay);
        data.put("todaySales", todaySales != null ? todaySales : 0.0);
        Double monthlyRevenue = saleRepository.getRevenueBetween(startOfMonth, endOfDay);
        data.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0.0);
        data.put("todayTransactions", saleRepository.countSalesBetween(startOfDay, endOfDay));
        data.put("totalMedicines", medicineRepository.count());
        data.put("totalCustomers", customerRepository.count());
        data.put("lowStockCount", medicineRepository.countByQuantityLessThan(10));

        // Expiry window counts
        LocalDate now = LocalDate.now();
        data.put("expiringIn30", medicineRepository.countByExpiryDateBetween(now, now.plusDays(30)));
        data.put("expiringIn60", medicineRepository.countByExpiryDateBetween(now.plusDays(31), now.plusDays(60)));
        data.put("expiringIn90", medicineRepository.countByExpiryDateBetween(now.plusDays(61), now.plusDays(90)));
        data.put("expiredCount",
                medicineRepository.countByExpiryDateBetween(LocalDate.of(2000, 1, 1), now.minusDays(1)));

        // Critical stock alerts (top items)
        List<Medicine> criticalStock = medicineRepository.findCriticalLowStock(10);
        data.put("criticalStockItems", criticalStock.stream().limit(8).collect(Collectors.toList()));

        // Charts — 7-day sales trend
        data.put("salesTrendJson", buildSalesTrendJson(saleRepository.getDailySalesTotals(sevenDaysAgo)));

        // Charts — category pie
        data.put("categoryJson", buildCategoryJson(saleRepository.getSalesByCategory(startOfMonth, endOfDay)));

        // Recent transactions (top 10)
        List<Sale> recent = saleRepository.findTop10WithDetails();
        data.put("recentSales", recent.stream().limit(10).collect(Collectors.toList()));

        // Activity History
        data.put("recentMedicines", medicineRepository.findTop5ByOrderByCreatedDateDesc());
        data.put("recentCustomers", customerRepository.findTop5ByOrderByRegisteredDateDesc());

        return data;
    }

    /** Branch-scoped dashboard (SHOPKEEPER) */
    @Cacheable(value = "dashboard_kpis", key = "#branchId")
    public Map<String, Object> buildBranchDashboard(Long branchId) {
        Map<String, Object> data = new LinkedHashMap<>();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();

        Double todaySales = saleRepository.getTotalSalesByBranchBetween(branchId, startOfDay, endOfDay);
        data.put("todaySales", todaySales != null ? todaySales : 0.0);
        Double monthlyRevenue = saleRepository.getTotalSalesByBranchBetween(branchId, startOfMonth, endOfDay);
        data.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0.0);
        data.put("totalMedicines", medicineRepository.countByBranchId(branchId));
        data.put("totalCustomers", customerRepository.countByBranchId(branchId));
        data.put("lowStockCount", medicineRepository.countByBranchIdAndQuantityLessThan(branchId, 10));

        LocalDate now = LocalDate.now();
        data.put("expiringIn30",
                medicineRepository.countByBranchIdAndExpiryDateBetween(branchId, now, now.plusDays(30)));
        data.put("expiringIn60",
                medicineRepository.countByBranchIdAndExpiryDateBetween(branchId, now.plusDays(31), now.plusDays(60)));
        data.put("expiringIn90",
                medicineRepository.countByBranchIdAndExpiryDateBetween(branchId, now.plusDays(61), now.plusDays(90)));

        List<Medicine> criticalStock = medicineRepository.findCriticalLowStockByBranch(branchId, 10);
        data.put("criticalStockItems", criticalStock.stream().limit(8).collect(Collectors.toList()));

        data.put("salesTrendJson",
                buildSalesTrendJson(saleRepository.getDailySalesTotalsByBranch(branchId, sevenDaysAgo)));
        data.put("categoryJson",
                buildCategoryJson(saleRepository.getSalesByCategoryByBranch(branchId, startOfMonth, endOfDay)));

        List<Sale> recent = saleRepository.findTop10WithDetailsByBranch(branchId);
        data.put("recentSales", recent.stream().limit(10).collect(Collectors.toList()));

        // Activity History
        data.put("recentMedicines", medicineRepository.findTop5ByBranchIdOrderByCreatedDateDesc(branchId));
        data.put("recentCustomers", customerRepository.findTop5ByBranchIdOrderByRegisteredDateDesc(branchId));

        return data;
    }

    /** Owner-scoped dashboard (OWNER sees all their branches combined) */
    @Cacheable(value = "dashboard_kpis", key = "'owner-' + #ownerId")
    public Map<String, Object> buildOwnerDashboard(Long ownerId) {
        Map<String, Object> data = new LinkedHashMap<>();

        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);
        LocalDateTime startOfMonth = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime sevenDaysAgo = LocalDate.now().minusDays(6).atStartOfDay();

        Double todaySales = saleRepository.getTotalSalesByOwnerBetween(ownerId, startOfDay, endOfDay);
        data.put("todaySales", todaySales != null ? todaySales : 0.0);
        Double monthlyRevenue = saleRepository.getTotalSalesByOwnerBetween(ownerId, startOfMonth, endOfDay);
        data.put("monthlyRevenue", monthlyRevenue != null ? monthlyRevenue : 0.0);
        data.put("totalMedicines", medicineRepository.countByOwnerId(ownerId));
        data.put("totalCustomers", customerRepository.countByOwnerId(ownerId));
        data.put("lowStockCount", medicineRepository.countLowStockByOwnerId(ownerId, 10));

        LocalDate now = LocalDate.now();
        data.put("expiringIn30", medicineRepository.countExpiringByOwner(ownerId, now, now.plusDays(30)));
        data.put("expiringIn60", medicineRepository.countExpiringByOwner(ownerId, now.plusDays(31), now.plusDays(60)));
        data.put("expiringIn90", medicineRepository.countExpiringByOwner(ownerId, now.plusDays(61), now.plusDays(90)));

        List<Medicine> criticalStock = medicineRepository.findCriticalLowStockByOwner(ownerId, 10);
        data.put("criticalStockItems", criticalStock.stream().limit(8).collect(Collectors.toList()));

        data.put("salesTrendJson",
                buildSalesTrendJson(saleRepository.getDailySalesTotalsByOwner(ownerId, sevenDaysAgo)));
        data.put("categoryJson",
                buildCategoryJson(saleRepository.getSalesByCategoryByOwner(ownerId, startOfMonth, endOfDay)));

        List<Sale> recent = saleRepository.findTop10WithDetailsByOwner(ownerId);
        data.put("recentSales", recent.stream().limit(10).collect(Collectors.toList()));

        org.springframework.data.domain.Pageable top5 = org.springframework.data.domain.PageRequest.of(0, 5);
        data.put("recentMedicines", medicineRepository.findTop5ByOwnerIdOrderByCreatedDateDesc(ownerId, top5));
        data.put("recentCustomers", customerRepository.findTop5ByOwnerIdOrderByRegisteredDateDesc(ownerId, top5));

        return data;
    }

    // ═══════════════════════════════════════════════════════════════════

    private String buildSalesTrendJson(List<Object[]> rows) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMM");
        // Fill 7-day grid so empty days show 0
        Map<LocalDate, Double> dayMap = new LinkedHashMap<>();
        for (int i = 6; i >= 0; i--) {
            dayMap.put(LocalDate.now().minusDays(i), 0.0);
        }
        if (rows != null) {
            for (Object[] row : rows) {
                LocalDate d = (LocalDate) row[0];
                Double v = row[1] != null ? ((Number) row[1]).doubleValue() : 0.0;
                if (dayMap.containsKey(d))
                    dayMap.put(d, v);
            }
        }
        StringBuilder sb = new StringBuilder();
        sb.append("{\"labels\":[");
        sb.append(dayMap.keySet().stream().map(d -> "\"" + d.format(fmt) + "\"").collect(Collectors.joining(",")));
        sb.append("],\"data\":[");
        sb.append(dayMap.values().stream().map(v -> String.format("%.2f", v)).collect(Collectors.joining(",")));
        sb.append("]}");
        return sb.toString();
    }

    private String buildCategoryJson(List<Object[]> rows) {
        if (rows == null || rows.isEmpty()) {
            return "{\"labels\":[],\"data\":[]}";
        }
        List<Object[]> top = rows.stream().limit(8).collect(Collectors.toList());
        StringBuilder sb = new StringBuilder();
        sb.append("{\"labels\":[");
        sb.append(top.stream().map(r -> "\"" + (r[0] != null ? r[0].toString() : "Other") + "\"")
                .collect(Collectors.joining(",")));
        sb.append("],\"data\":[");
        sb.append(top.stream().map(r -> r[1] != null ? String.format("%.2f", ((Number) r[1]).doubleValue()) : "0")
                .collect(Collectors.joining(",")));
        sb.append("]}");
        return sb.toString();
    }
}
