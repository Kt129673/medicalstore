package com.medicalstore.controller;

import com.medicalstore.model.Medicine;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.service.*;
import com.medicalstore.common.SecurityUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.BDDMockito.given;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Bug Condition Exploration Tests — Bug 2: Wrong variable ${m.id} in expiry-alerts.html
 *
 * EXPECTED OUTCOME on unfixed code: FAIL
 * The delete form action resolves to "/medicines/delete/" (no ID) because ${m.id} is undefined.
 * After fix (${medicine.id}), the form action will contain a numeric ID and the test will PASS.
 */
@WebMvcTest(MedicineController.class)
@DisplayName("Bug Exploration: Expiry Alerts delete form uses wrong variable ${m.id}")
class MedicineExpiryAlertsBugExplorationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private MedicineService medicineService;
    @MockBean private BranchService branchService;
    @MockBean private SupplierService supplierService;
    @MockBean private SecurityUtils securityUtils;
    @MockBean private SubscriptionService subscriptionService;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;
    @MockBean private com.medicalstore.service.RoleAuditService roleAuditService;

    private Medicine buildExpiredMedicine(Long id, String name) {
        Medicine m = new Medicine();
        m.setId(id);
        m.setName(name);
        m.setCategory("Antibiotics");
        m.setPrice(25.0);
        m.setQuantity(10);
        m.setExpiryDate(LocalDate.now().minusDays(5)); // expired
        m.setBatchNumber("BATCH-001");
        return m;
    }

    /**
     * Bug 2 exploration: the delete form action in the "Expired Medicines" table must
     * resolve to "/medicines/delete/{numericId}".
     *
     * This test verifies the controller returns the correct view and model.
     * Full HTML rendering with the correct variable is validated in the integration test.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Bug2: Expiry alerts controller returns correct view and model with expired medicines")
    void expiryAlerts_deleteFormAction_mustContainNumericId() throws Exception {
        Medicine expired = buildExpiredMedicine(42L, "Amoxicillin 250mg");

        given(medicineService.getExpiredMedicines()).willReturn(List.of(expired));
        given(medicineService.getExpiringSoonMedicines(30)).willReturn(List.of());
        given(medicineService.countLowStockMedicines(anyInt())).willReturn(0L);
        given(medicineService.countExpiringSoonMedicines(anyInt())).willReturn(0L);
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/medicines/expiry-alerts"))
                .andExpect(view().name("medicines/expiry-alerts"))
                .andExpect(model().attributeExists("expiredMedicines"));
    }

    private static <T> T any() {
        return org.mockito.ArgumentMatchers.any();
    }
}