package com.medicalstore.service;

import com.medicalstore.repository.CustomerRepository;
import com.medicalstore.repository.MedicineRepository;
import com.medicalstore.repository.SaleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

/**
 * Preservation Property Tests — Property 2: Non-Buggy Inputs Unchanged
 *
 * These tests MUST PASS on unfixed code (baseline) and MUST CONTINUE TO PASS after fixes.
 * They guard against regressions: existing keys must remain present and correct.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Preservation: Dashboard non-buggy keys must remain unchanged after fix")
class DashboardServicePreservationTest {

    @Mock private SaleRepository saleRepository;
    @Mock private MedicineRepository medicineRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks
    private DashboardService dashboardService;

    // ── Stub helpers ──────────────────────────────────────────────────────────

    private void stubAdminDashboard() {
        when(saleRepository.getRevenueBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0.0);
        when(saleRepository.countSalesBetween(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(0L);
        when(medicineRepository.count()).thenReturn(0L);
        when(customerRepository.count()).thenReturn(0L);
        when(medicineRepository.countByQuantityLessThan(anyInt())).thenReturn(0L);
        when(medicineRepository.countByExpiryDateBetween(any(LocalDate.class), any(LocalDate.class))).thenReturn(0L);
        when(medicineRepository.findCriticalLowStock(anyInt())).thenReturn(List.of());
        when(saleRepository.getDailySalesTotals(any(LocalDateTime.class))).thenReturn(List.of());
        when(saleRepository.getSalesByCategory(any(LocalDateTime.class), any(LocalDateTime.class))).thenReturn(List.of());
        when(saleRepository.findTop10WithDetails()).thenReturn(List.of());
        when(medicineRepository.findTop5ByOrderByCreatedDateDesc()).thenReturn(List.of());
        when(customerRepository.findTop5ByOrderByRegisteredDateDesc()).thenReturn(List.of());
    }

    private void stubBranchDashboard(Long branchId) {
        when(saleRepository.getTotalSalesByBranchBetween(eq(branchId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0.0);
        when(medicineRepository.countByBranchId(branchId)).thenReturn(0L);
        when(customerRepository.countByBranchId(branchId)).thenReturn(0L);
        when(medicineRepository.countByBranchIdAndQuantityLessThan(eq(branchId), anyInt())).thenReturn(0L);
        when(medicineRepository.countByBranchIdAndExpiryDateBetween(eq(branchId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0L);
        when(medicineRepository.findCriticalLowStockByBranch(eq(branchId), anyInt())).thenReturn(List.of());
        when(saleRepository.getDailySalesTotalsByBranch(eq(branchId), any(LocalDateTime.class))).thenReturn(List.of());
        when(saleRepository.getSalesByCategoryByBranch(eq(branchId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(saleRepository.findTop10WithDetailsByBranch(branchId)).thenReturn(List.of());
        when(medicineRepository.findTop5ByBranchIdOrderByCreatedDateDesc(branchId)).thenReturn(List.of());
        when(customerRepository.findTop5ByBranchIdOrderByRegisteredDateDesc(branchId)).thenReturn(List.of());
    }

    private void stubOwnerDashboard(Long ownerId) {
        when(saleRepository.getTotalSalesByOwnerBetween(eq(ownerId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(0.0);
        when(medicineRepository.countByOwnerId(ownerId)).thenReturn(0L);
        when(customerRepository.countByOwnerId(ownerId)).thenReturn(0L);
        when(medicineRepository.countLowStockByOwnerId(eq(ownerId), anyInt())).thenReturn(0L);
        when(medicineRepository.countExpiringByOwner(eq(ownerId), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(0L);
        when(medicineRepository.findCriticalLowStockByOwner(eq(ownerId), anyInt())).thenReturn(List.of());
        when(saleRepository.getDailySalesTotalsByOwner(eq(ownerId), any(LocalDateTime.class))).thenReturn(List.of());
        when(saleRepository.getSalesByCategoryByOwner(eq(ownerId), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(List.of());
        when(saleRepository.findTop10WithDetailsByOwner(ownerId)).thenReturn(List.of());
        when(medicineRepository.findTop5ByOwnerIdOrderByCreatedDateDesc(eq(ownerId), any())).thenReturn(List.of());
        when(customerRepository.findTop5ByOwnerIdOrderByRegisteredDateDesc(eq(ownerId), any())).thenReturn(List.of());
    }

    // ── Admin dashboard: expiredCount must still be present (regression guard) ──

    @Test
    @DisplayName("Preservation [ADMIN]: buildAdminDashboard still contains 'expiredCount' after fix")
    void adminDashboard_expiredCount_stillPresent() {
        stubAdminDashboard();

        Map<String, Object> data = dashboardService.buildAdminDashboard();

        assertThat(data).containsKey("expiredCount");
    }

    // ── Branch dashboard: non-buggy keys must be preserved ───────────────────

    @ParameterizedTest(name = "branchId={0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("Preservation [BRANCH]: non-buggy keys always present in buildBranchDashboard")
    void branchDashboard_nonBuggyKeys_alwaysPresent(Long branchId) {
        stubBranchDashboard(branchId);

        Map<String, Object> data = dashboardService.buildBranchDashboard(branchId);

        assertThat(data).containsKeys(
                "todaySales", "monthlyRevenue", "totalMedicines",
                "lowStockCount", "expiringIn30", "expiringIn60", "expiringIn90"
        );
    }

    // ── Owner dashboard: non-buggy keys must be preserved ────────────────────

    @ParameterizedTest(name = "ownerId={0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("Preservation [OWNER]: non-buggy keys always present in buildOwnerDashboard")
    void ownerDashboard_nonBuggyKeys_alwaysPresent(Long ownerId) {
        stubOwnerDashboard(ownerId);

        Map<String, Object> data = dashboardService.buildOwnerDashboard(ownerId);

        assertThat(data).containsKeys(
                "todaySales", "monthlyRevenue", "totalMedicines",
                "lowStockCount", "expiringIn30", "expiringIn60", "expiringIn90"
        );
    }

    // ── expiredCount=0 must be 0L (not absent) — JS falsy check hides card ───

    @Test
    @DisplayName("Preservation [BRANCH]: expiredCount=0 is exactly 0L so JS hides the card")
    void branchDashboard_expiredCountZero_isZeroLong() {
        stubBranchDashboard(1L);
        // countByBranchIdAndExpiryDateBetween already returns 0L from stub

        Map<String, Object> data = dashboardService.buildBranchDashboard(1L);

        // After fix: key present with value 0L — JS `if (data.expiredCount)` evaluates false → card hidden
        if (data.containsKey("expiredCount")) {
            assertThat(data.get("expiredCount")).isEqualTo(0L);
        }
        // If key absent (unfixed), this test still passes — it's a preservation test, not an exploration test
    }
}
