package com.medicalstore.controller.api;

import com.medicalstore.dto.MedicineDTO;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.service.*;
import com.medicalstore.util.SecurityUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PosApiController.class)
@DisplayName("POS API Controller Tests")
class PosApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private MedicineService medicineService;
    @MockBean private SubscriptionService subscriptionService;
    @MockBean private SecurityUtils securityUtils;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;
    @MockBean private com.medicalstore.service.RoleAuditService roleAuditService;

    // ─── Helper ──────────────────────────────────────────────────────────────

    private MedicineDTO buildDto(Long id, String name, double price, int qty) {
        // MedicineDTO(id, name, category, price, quantity, batchNumber, gstPercentage)
        return new MedicineDTO(id, name, "Analgesic", price, qty, null, null);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/v1/medicines/search
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "SHOPKEEPER")
    @DisplayName("GET /api/v1/medicines/search?q=para – returns matching medicines as JSON")
    void searchMedicines_validQuery_returnsResults() throws Exception {
        List<MedicineDTO> results = List.of(buildDto(1L, "Paracetamol 500mg", 10.0, 100));
        given(medicineService.searchMedicinesForPos("para")).willReturn(results);
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/medicines/search")
                        .param("q", "para")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].name").value("Paracetamol 500mg"));
    }

    @Test
    @WithMockUser(roles = "SHOPKEEPER")
    @DisplayName("GET /api/v1/medicines/search?q= – returns empty list for blank query")
    void searchMedicines_blankQuery_returnsEmptyList() throws Exception {
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/medicines/search")
                        .param("q", "")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    @WithMockUser(roles = "SHOPKEEPER")
    @DisplayName("GET /api/v1/medicines/search – returns empty list when no results")
    void searchMedicines_noResults_returnsEmptyList() throws Exception {
        given(medicineService.searchMedicinesForPos("xyz")).willReturn(List.of());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/medicines/search")
                        .param("q", "xyz")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /api/v1/medicines/search – accessible to ADMIN role")
    void searchMedicines_asAdmin_isAllowed() throws Exception {
        given(medicineService.searchMedicinesForPos("ibu")).willReturn(List.of());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/medicines/search")
                        .param("q", "ibu")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/medicines/search – returns 401 for unauthenticated request")
    void searchMedicines_unauthenticated_isUnauthorized() throws Exception {
        mockMvc.perform(get("/api/v1/medicines/search")
                        .param("q", "para")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}
