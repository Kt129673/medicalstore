package com.medicalstore.controller;

import com.medicalstore.model.Customer;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.service.*;
import com.medicalstore.common.SecurityUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CustomerController.class)
@DisplayName("CustomerController UI Tests")
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean private CustomerService customerService;
    @MockBean private SecurityUtils securityUtils;
    @MockBean private BranchService branchService;
    @MockBean private SaleService saleService;
    @MockBean private MedicineService medicineService;
    @MockBean private SubscriptionService subscriptionService;
    @MockBean private CustomUserDetailsService customUserDetailsService;
    @MockBean private UserRepository userRepository;
    @MockBean private com.medicalstore.service.RoleAuditService roleAuditService;

    // ─── Helper ──────────────────────────────────────────────────────────────

    private Customer buildCustomer(Long id, String name, String phone) {
        Customer c = new Customer();
        c.setId(id);
        c.setName(name);
        c.setPhone(phone);
        c.setLoyaltyPoints(0);
        return c;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /customers  (list)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /customers – renders customer list with all customers")
    void listCustomers_asAdmin_returnsListView() throws Exception {
        given(customerService.getAllCustomers()).willReturn(List.of(
                buildCustomer(1L, "Alice", "9000000001"),
                buildCustomer(2L, "Bob", "9000000002")));
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/list"))
                .andExpect(model().attributeExists("customers"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /customers?search=ali – returns filtered customer list")
    void listCustomers_withSearch_returnsFilteredList() throws Exception {
        given(customerService.searchCustomers("ali")).willReturn(
                List.of(buildCustomer(1L, "Alice", "9000000001")));
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/customers").param("search", "ali"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/list"))
                .andExpect(model().attributeExists("customers"));

        verify(customerService).searchCustomers("ali");
    }

    @Test
    @DisplayName("GET /customers – returns 401 for unauthenticated user")
    void listCustomers_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/customers"))
                .andExpect(status().isUnauthorized());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /customers/{id}  (view)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /customers/{id} – shows customer detail page")
    void viewCustomer_existing_returnsView() throws Exception {
        Customer customer = buildCustomer(1L, "Alice", "9000000001");
        given(customerService.getCustomerById(1L)).willReturn(Optional.of(customer));
        given(saleService.getSalesByCustomer(1L)).willReturn(List.of());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/customers/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/view"))
                .andExpect(model().attribute("customer", customer))
                .andExpect(model().attributeExists("purchases"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /customers/{id} – throws error when customer not found")
    void viewCustomer_notFound_returnsError() throws Exception {
        given(customerService.getCustomerById(999L)).willReturn(Optional.empty());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        // Controller throws RuntimeException → GlobalExceptionHandler returns 500
        mockMvc.perform(get("/customers/999"))
                .andExpect(status().is5xxServerError());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /customers/new
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /customers/new – renders customer add form")
    void showAddForm_asAdmin_rendersForm() throws Exception {
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/customers/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/form"))
                .andExpect(model().attributeExists("customer"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /customers/edit/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /customers/edit/{id} – renders edit form with customer data")
    void showEditForm_existingCustomer_rendersFormWithData() throws Exception {
        Customer customer = buildCustomer(1L, "Alice", "9000000001");
        given(customerService.getCustomerById(1L)).willReturn(Optional.of(customer));
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/customers/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("customers/form"))
                .andExpect(model().attribute("customer", customer));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /customers/save
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /customers/save – creates customer and redirects to list")
    void saveCustomer_newCustomer_redirectsToList() throws Exception {
        Customer saved = buildCustomer(5L, "New Customer", "9000000099");
        given(customerService.saveCustomer(any(Customer.class))).willReturn(saved);
        given(securityUtils.isShopkeeper()).willReturn(false);
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/customers/save")
                        .with(csrf())
                        .param("name", "New Customer")
                        .param("phone", "9000000099"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
    }

    @Test
    @WithMockUser(roles = "SHOPKEEPER")
    @DisplayName("POST /customers/save – SHOPKEEPER saves customer with auto branch assignment")
    void saveCustomer_shopkeeper_assignsBranchAutomatically() throws Exception {
        Customer saved = buildCustomer(6L, "Shop Customer", "9000000088");
        given(customerService.saveCustomer(any(Customer.class))).willReturn(saved);
        given(securityUtils.isShopkeeper()).willReturn(true);
        given(securityUtils.getCurrentBranchId()).willReturn(2L);
        given(branchService.getBranchById(2L)).willReturn(Optional.empty());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/customers/save")
                        .with(csrf())
                        .param("name", "Shop Customer")
                        .param("phone", "9000000088"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /customers/delete/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /customers/delete/{id} – deletes customer and redirects")
    void deleteCustomer_asAdmin_redirectsToList() throws Exception {
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/customers/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customers"));

        verify(customerService).deleteCustomer(1L);
    }

    @Test
    @WithMockUser(roles = "SHOPKEEPER")
    @DisplayName("POST /customers/delete/{id} – SHOPKEEPER gets 403 (ADMIN only)")
    void deleteCustomer_asShopkeeper_returns403() throws Exception {
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/customers/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection()); // redirected to denied page by AccessDeniedHandler
    }
}
