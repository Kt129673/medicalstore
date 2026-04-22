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
 * Bug Condition Exploration Tests — Property 1: Bug Condition
 *
 * These tests encode the EXPECTED (correct) behavior.
 * They MUST FAIL on unfixed code — failure confirms each bug exists.
 * After fixes are applied, these tests MUST PASS.
 *
 * Bug 4: buildBranchDashboard and buildOwnerDashboard must include "expiredCount" key.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Bug Exploration: Dashboard expiredCount missing for non-Admin roles")
class DashboardServiceBugExplorationTest {

    @Mock private SaleRepository saleRepository;
    @Mock private MedicineRepository medicineRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks
    private DashboardService dashboardService;

    // ── Stub helpers ──────────────────────────────────────────────────────────

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

    // ── Bug 4: buildBranchDashboard must contain "expiredCount" ──────────────

    @Test
    @DisplayName("Bug4 [BRANCH]: buildBranchDashboard map must contain 'expiredCount' key")
    void branchDashboard_mustContainExpiredCount() {
        stubBranchDashboard(1L);

        Map<String, Object> data = dashboardService.buildBranchDashboard(1L);

        // FAILS on unfixed code — key is absent in buildBranchDashboard
        assertThat(data).containsKey("expiredCount");
    }

    @Test
    @DisplayName("Bug4 [OWNER]: buildOwnerDashboard map must contain 'expiredCount' key")
    void ownerDashboard_mustContainExpiredCount() {
        stubOwnerDashboard(1L);

        Map<String, Object> data = dashboardService.buildOwnerDashboard(1L);

        // FAILS on unfixed code — key is absent in buildOwnerDashboard
        assertThat(data).containsKey("expiredCount");
    }

    // ── Scoped PBT: multiple branch IDs ──────────────────────────────────────

    @ParameterizedTest(name = "branchId={0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("Bug4 [PBT-BRANCH]: for any branchId, buildBranchDashboard must contain 'expiredCount'")
    void branchDashboard_forAnyBranchId_mustContainExpiredCount(Long branchId) {
        stubBranchDashboard(branchId);

        Map<String, Object> data = dashboardService.buildBranchDashboard(branchId);

        // Generates counterexample on unfixed code
        assertThat(data).containsKey("expiredCount");
    }

    @ParameterizedTest(name = "ownerId={0}")
    @ValueSource(longs = {1L, 2L, 3L})
    @DisplayName("Bug4 [PBT-OWNER]: for any ownerId, buildOwnerDashboard must contain 'expiredCount'")
    void ownerDashboard_forAnyOwnerId_mustContainExpiredCount(Long ownerId) {
        stubOwnerDashboard(ownerId);

        Map<String, Object> data = dashboardService.buildOwnerDashboard(ownerId);

        assertThat(data).containsKey("expiredCount");
    }
}
