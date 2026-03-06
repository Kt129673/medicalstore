package com.medicalstore.controller.api;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

/**
 * Integration tests for Medicine REST API endpoints.
 * Uses H2 in-memory database and Spring Security mock users.
 */
@SpringBootTest
@AutoConfigureMockMvc
class MedicineApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Nested
    @DisplayName("GET /api/v1/medicines")
    class ListMedicines {

        @Test
        @DisplayName("Should return 200 with list for ADMIN")
        @WithMockUser(roles = "ADMIN")
        void admin_shouldAccessMedicineList() throws Exception {
            mockMvc.perform(get("/api/v1/medicines")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should return 200 with list for SHOPKEEPER")
        @WithMockUser(roles = "SHOPKEEPER")
        void shopkeeper_shouldAccessMedicineList() throws Exception {
            mockMvc.perform(get("/api/v1/medicines")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 403 for OWNER (no access to medicines)")
        @WithMockUser(roles = "OWNER")
        void owner_shouldBeDeniedMedicineList() throws Exception {
            mockMvc.perform(get("/api/v1/medicines")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should return 401/302 for unauthenticated user")
        void unauthenticated_shouldBeRedirected() throws Exception {
            mockMvc.perform(get("/api/v1/medicines")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().is3xxRedirection());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/medicines/count")
    class CountMedicines {

        @Test
        @DisplayName("Should return count as JSON")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnCount() throws Exception {
            mockMvc.perform(get("/api/v1/medicines/count")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").isNumber());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/medicines/{id}")
    class GetMedicineById {

        @Test
        @DisplayName("Should return 404 for non-existent medicine")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404ForMissingMedicine() throws Exception {
            mockMvc.perform(get("/api/v1/medicines/99999")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/medicines/low-stock")
    class LowStock {

        @Test
        @DisplayName("Should return low stock list with default threshold")
        @WithMockUser(roles = "SHOPKEEPER")
        void shouldReturnLowStockMedicines() throws Exception {
            mockMvc.perform(get("/api/v1/medicines/low-stock")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should accept custom threshold parameter")
        @WithMockUser(roles = "ADMIN")
        void shouldAcceptCustomThreshold() throws Exception {
            mockMvc.perform(get("/api/v1/medicines/low-stock")
                    .param("threshold", "5")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/medicines/categories")
    class Categories {

        @Test
        @DisplayName("Should return list of categories")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnCategories() throws Exception {
            mockMvc.perform(get("/api/v1/medicines/categories")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/medicines/expired")
    class Expired {

        @Test
        @DisplayName("Should return expired medicines")
        @WithMockUser(roles = "SHOPKEEPER")
        void shouldReturnExpiredMedicines() throws Exception {
            mockMvc.perform(get("/api/v1/medicines/expired")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }
}
