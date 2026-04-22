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
import org.springframework.data.domain.PageImpl;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Preservation Tests — Non-Buggy Inputs Unchanged
 *
 * Verifies that the controller layer returns the correct view names and model
 * attributes after all fixes. Full HTML rendering is covered by integration tests.
 */
@WebMvcTest(MedicineController.class)
@DisplayName("Preservation: Non-buggy medicine page behaviors must remain unchanged")
class MedicineListPreservationTest {

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

    private Medicine buildMedicine(Long id, String name, String category) {
        Medicine m = new Medicine();
        m.setId(id);
        m.setName(name);
        m.setCategory(category);
        m.setPrice(10.0);
        m.setQuantity(50);
        m.setExpiryDate(LocalDate.now().plusMonths(6));
        return m;
    }

    /**
     * Preservation 3.1: GET /medicines controller returns correct view and model.
     * The delete form variable fix must not affect the controller layer.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Preservation: GET /medicines returns medicines/list view with medicinePage model")
    void medicineList_asAdmin_returnsCorrectViewAndModel() throws Exception {
        given(medicineService.filterMedicines(any(), any(), any(), any(), any()))
                .willReturn(new PageImpl<>(List.of(buildMedicine(7L, "Paracetamol 500mg", "Analgesic"))));
        given(medicineService.getAllCategories()).willReturn(List.of("Analgesic"));
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/medicines"))
                .andExpect(view().name("medicines/list"))
                .andExpect(model().attributeExists("medicinePage", "categories"));
    }

    /**
     * Preservation 3.2: GET /medicines/expiry-alerts controller returns correct view and model.
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("Preservation: GET /medicines/expiry-alerts returns correct view and model")
    void expiryAlerts_returnsCorrectViewAndModel() throws Exception {
        given(medicineService.getExpiredMedicines()).willReturn(List.of());
        given(medicineService.getExpiringSoonMedicines(30)).willReturn(List.of());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/medicines/expiry-alerts"))
                .andExpect(view().name("medicines/expiry-alerts"))
                .andExpect(model().attributeExists("expiredMedicines", "expiringSoonMedicines"));
    }
}
