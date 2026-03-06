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
 * Integration tests for Sales, Customers, Suppliers, Analytics, and AppInfo
 * REST APIs.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApiEndpointsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Sales API ────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Sales API - /api/v1/sales")
    class SalesApi {

        @Test
        @DisplayName("GET /api/v1/sales — paginated list for ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnPaginatedSales() throws Exception {
            mockMvc.perform(get("/api/v1/sales")
                    .param("page", "0")
                    .param("size", "5")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").isNumber());
        }

        @Test
        @DisplayName("GET /api/v1/sales/today — returns today's total")
        @WithMockUser(roles = "SHOPKEEPER")
        void shouldReturnTodaySalesTotal() throws Exception {
            mockMvc.perform(get("/api/v1/sales/today")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.todaySalesTotal").isNumber());
        }

        @Test
        @DisplayName("GET /api/v1/sales/recent — returns recent sales")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnRecentSales() throws Exception {
            mockMvc.perform(get("/api/v1/sales/recent")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("OWNER should be denied access to sales API")
        @WithMockUser(roles = "OWNER")
        void owner_shouldBeDeniedSalesAccess() throws Exception {
            mockMvc.perform(get("/api/v1/sales")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isForbidden());
        }
    }

    // ── Customers API ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Customers API - /api/v1/customers")
    class CustomersApi {

        @Test
        @DisplayName("GET /api/v1/customers — list for ADMIN")
        @WithMockUser(roles = "ADMIN")
        void shouldListCustomers() throws Exception {
            mockMvc.perform(get("/api/v1/customers")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/customers/count — returns count")
        @WithMockUser(roles = "SHOPKEEPER")
        void shouldReturnCustomerCount() throws Exception {
            mockMvc.perform(get("/api/v1/customers/count")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.count").isNumber());
        }

        @Test
        @DisplayName("GET /api/v1/customers/search — search by name")
        @WithMockUser(roles = "ADMIN")
        void shouldSearchCustomers() throws Exception {
            mockMvc.perform(get("/api/v1/customers/search")
                    .param("q", "test")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("GET /api/v1/customers/{id} — 404 for missing customer")
        @WithMockUser(roles = "ADMIN")
        void shouldReturn404ForMissingCustomer() throws Exception {
            mockMvc.perform(get("/api/v1/customers/99999")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    // ── Suppliers API ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Suppliers API - /api/v1/suppliers")
    class SuppliersApi {

        @Test
        @DisplayName("GET /api/v1/suppliers — list for SHOPKEEPER")
        @WithMockUser(roles = "SHOPKEEPER")
        void shouldListSuppliers() throws Exception {
            mockMvc.perform(get("/api/v1/suppliers")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/suppliers/search — search by name")
        @WithMockUser(roles = "ADMIN")
        void shouldSearchSuppliers() throws Exception {
            mockMvc.perform(get("/api/v1/suppliers/search")
                    .param("q", "pharma")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    // ── Analytics API ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Analytics API - /api/v1/analytics")
    class AnalyticsApi {

        @Test
        @DisplayName("GET /api/v1/analytics/dead-stock — returns dead stock")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnDeadStock() throws Exception {
            mockMvc.perform(get("/api/v1/analytics/dead-stock")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/analytics/fast-moving — returns fast movers")
        @WithMockUser(roles = "OWNER")
        void shouldReturnFastMoving() throws Exception {
            mockMvc.perform(get("/api/v1/analytics/fast-moving")
                    .param("limit", "5")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("GET /api/v1/analytics/gst-summary — returns GST data")
        @WithMockUser(roles = "SHOPKEEPER")
        void shouldReturnGstSummary() throws Exception {
            mockMvc.perform(get("/api/v1/analytics/gst-summary")
                    .param("year", "2025")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    // ── App Info API ─────────────────────────────────────────────────────────

    @Nested
    @DisplayName("App Info - /api/v1/info")
    class AppInfoApi {

        @Test
        @DisplayName("GET /api/v1/info — returns app metadata")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnAppInfo() throws Exception {
            mockMvc.perform(get("/api/v1/info")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("MedicalStore"))
                    .andExpect(jsonPath("$.version").value("1.0.0"))
                    .andExpect(jsonPath("$.java").isNotEmpty())
                    .andExpect(jsonPath("$.springBoot").isNotEmpty());
        }
    }

    // ── Dashboard API ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Dashboard KPIs - /api/v1/dashboard/kpis")
    class DashboardApi {

        @Test
        @DisplayName("GET /api/v1/dashboard/kpis — returns KPI data")
        @WithMockUser(roles = "ADMIN")
        void shouldReturnDashboardKpis() throws Exception {
            mockMvc.perform(get("/api/v1/dashboard/kpis")
                    .accept(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }
    }
}
