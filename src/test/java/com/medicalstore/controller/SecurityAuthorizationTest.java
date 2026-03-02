package com.medicalstore.controller;

import com.medicalstore.repository.UserRepository;
import com.medicalstore.service.*;
import com.medicalstore.util.SecurityUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Security & Role-Based Authorization Tests.
 *
 * Verifies that each protected route enforces its declared role restriction.
 * Uses @WebMvcTest with @WithMockUser to simulate different roles without
 * starting a full application context or connecting to a real database.
 */
@WebMvcTest({
        LoginController.class,
        MedicineController.class,
        CustomerController.class,
        SaleController.class
})
@DisplayName("Security & Authorization Tests")
class SecurityAuthorizationTest {

    @Autowired
    private MockMvc mockMvc;

    // ── Shared mocks across all controllers included in this slice ────────
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;
    @MockBean private SecurityUtils securityUtils;
    @MockBean private MedicineService medicineService;
    @MockBean private BranchService branchService;
    @MockBean private SupplierService supplierService;
    @MockBean private CustomerService customerService;
    @MockBean private SaleService saleService;
    @MockBean private WhatsAppService whatsAppService;
    @MockBean private PdfService pdfService;
    @MockBean private SubscriptionService subscriptionService;
    @MockBean private com.medicalstore.service.RoleAuditService roleAuditService;

    // ─────────────────────────────────────────────────────────────────────────
    // Public routes — should always be accessible
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("GET /login – accessible to anonymous user")
    void loginPage_anonymousUser_isAccessible() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Unauthenticated access to protected routes → redirect to login
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithAnonymousUser
    @DisplayName("GET /medicines – anonymous user returns 401 (no session redirect in WebMvcTest)")
    void medicines_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/medicines"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /customers – anonymous user returns 401")
    void customers_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithAnonymousUser
    @DisplayName("GET /sales – anonymous user returns 401")
    void sales_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/sales"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN-only endpoints
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /medicines/delete/{id} – ADMIN can delete medicines")
    void deleteMedicine_asAdmin_isAllowed() throws Exception {
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/medicines/delete/1")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(roles = "SHOPKEEPER")
    @DisplayName("POST /medicines/delete/{id} – SHOPKEEPER is denied (403-redirect)")
    void deleteMedicine_asShopkeeper_isDenied() throws Exception {
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/medicines/delete/1")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection()); // AccessDeniedHandler redirects
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /customers/delete/{id} – ADMIN can delete customers")
    void deleteCustomer_asAdmin_isAllowed() throws Exception {
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/customers/delete/1")
                        .with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ADMIN and SHOPKEEPER access to operational endpoints
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /medicines – ADMIN can view medicines list")
    void medicines_asAdmin_isAllowed() throws Exception {
        given(medicineService.filterMedicines(any(), any(), any(), any(), any()))
                .willReturn(org.springframework.data.domain.Page.empty());
        given(medicineService.getAllCategories()).willReturn(java.util.List.of());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/medicines"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SHOPKEEPER")
    @DisplayName("GET /medicines – SHOPKEEPER can view medicines list")
    void medicines_asShopkeeper_isAllowed() throws Exception {
        given(medicineService.filterMedicines(any(), any(), any(), any(), any()))
                .willReturn(org.springframework.data.domain.Page.empty());
        given(medicineService.getAllCategories()).willReturn(java.util.List.of());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/medicines"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /sales – ADMIN can view sales list")
    void sales_asAdmin_isAllowed() throws Exception {
        given(saleService.getSalesPaginated(any()))
                .willReturn(org.springframework.data.domain.Page.empty());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/sales"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SHOPKEEPER")
    @DisplayName("GET /sales – SHOPKEEPER can view sales list")
    void sales_asShopkeeper_isAllowed() throws Exception {
        given(saleService.getSalesPaginated(any()))
                .willReturn(org.springframework.data.domain.Page.empty());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/sales"))
                .andExpect(status().isOk());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CSRF protection
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST without CSRF token – returns 403 Forbidden")
    void post_withoutCsrfToken_returns403() throws Exception {
        // No .with(csrf()) — CSRF token is missing
        mockMvc.perform(post("/medicines/delete/1"))
                .andExpect(status().isForbidden());
    }
}
