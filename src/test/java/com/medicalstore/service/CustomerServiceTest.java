package com.medicalstore.service;

import com.medicalstore.config.TenantContext;
import com.medicalstore.model.Customer;
import com.medicalstore.repository.CustomerRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CustomerService Unit Tests")
class CustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CustomerService customerService;

    @BeforeEach
    void clearContext() {
        TenantContext.clear();
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private Customer buildCustomer(Long id, String name, String phone, String email) {
        Customer c = new Customer();
        c.setId(id);
        c.setName(name);
        c.setPhone(phone);
        c.setEmail(email);
        c.setLoyaltyPoints(0);
        return c;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllCustomers
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllCustomers – returns all when no tenant context")
    void getAllCustomers_noContext_returnsAll() {
        List<Customer> list = List.of(
                buildCustomer(1L, "Alice", "9000000001", "alice@example.com"),
                buildCustomer(2L, "Bob", "9000000002", "bob@example.com"));
        when(customerRepository.findAll()).thenReturn(list);

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).hasSize(2);
        verify(customerRepository).findAll();
    }

    @Test
    @DisplayName("getAllCustomers – filters by branch when tenant context is set")
    void getAllCustomers_withTenantContext_returnsBranchCustomers() {
        TenantContext.setTenantId(2L);
        List<Customer> branchCustomers = List.of(buildCustomer(3L, "Charlie", "9000000003", null));
        when(customerRepository.findByBranchId(2L)).thenReturn(branchCustomers);

        List<Customer> result = customerService.getAllCustomers();

        assertThat(result).hasSize(1);
        verify(customerRepository).findByBranchId(2L);
        verify(customerRepository, never()).findAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getCustomerById
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getCustomerById – returns customer when found")
    void getCustomerById_found_returnsCustomer() {
        Customer c = buildCustomer(1L, "Alice", "9000000001", "alice@example.com");
        when(customerRepository.findById(1L)).thenReturn(Optional.of(c));

        Optional<Customer> result = customerService.getCustomerById(1L);

        assertThat(result).isPresent().contains(c);
    }

    @Test
    @DisplayName("getCustomerById – returns empty when not found")
    void getCustomerById_notFound_returnsEmpty() {
        when(customerRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Customer> result = customerService.getCustomerById(99L);

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // saveCustomer
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveCustomer – persists customer and returns saved entity")
    void saveCustomer_validCustomer_returnsSaved() {
        Customer c = buildCustomer(null, "Dave", "9000000004", "dave@example.com");
        Customer saved = buildCustomer(5L, "Dave", "9000000004", "dave@example.com");
        when(customerRepository.save(c)).thenReturn(saved);

        Customer result = customerService.saveCustomer(c);

        assertThat(result.getId()).isEqualTo(5L);
        verify(customerRepository).save(c);
    }

    @Test
    @DisplayName("saveCustomer – updates existing customer")
    void saveCustomer_existingCustomer_updates() {
        Customer c = buildCustomer(3L, "Eve Updated", "9000000005", null);
        when(customerRepository.save(c)).thenReturn(c);

        Customer result = customerService.saveCustomer(c);

        assertThat(result.getName()).isEqualTo("Eve Updated");
        verify(customerRepository).save(c);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteCustomer
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteCustomer – delegates to repository")
    void deleteCustomer_callsRepository() {
        customerService.deleteCustomer(10L);

        verify(customerRepository).deleteById(10L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // searchCustomers
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchCustomers – searches globally when no context")
    void searchCustomers_noContext_searchesGlobally() {
        List<Customer> found = List.of(buildCustomer(1L, "Alice", "9000000001", null));
        when(customerRepository.findByNameContainingIgnoreCase("ali")).thenReturn(found);

        List<Customer> result = customerService.searchCustomers("ali");

        assertThat(result).hasSize(1);
        verify(customerRepository).findByNameContainingIgnoreCase("ali");
    }

    @Test
    @DisplayName("searchCustomers – filters by branch when tenant context is set")
    void searchCustomers_withTenantContext_searchesByBranch() {
        TenantContext.setTenantId(4L);
        List<Customer> found = List.of(buildCustomer(2L, "Alicia", "9000000006", null));
        when(customerRepository.findByBranchIdAndNameContainingIgnoreCase(4L, "ali")).thenReturn(found);

        List<Customer> result = customerService.searchCustomers("ali");

        assertThat(result).hasSize(1);
        verify(customerRepository).findByBranchIdAndNameContainingIgnoreCase(4L, "ali");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // countAllCustomers
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("countAllCustomers – returns global count when no context")
    void countAllCustomers_noContext_returnsGlobalCount() {
        when(customerRepository.count()).thenReturn(55L);

        long count = customerService.countAllCustomers();

        assertThat(count).isEqualTo(55L);
    }

    @Test
    @DisplayName("countAllCustomers – returns branch count when tenant context is set")
    void countAllCustomers_withTenantContext_returnsBranchCount() {
        TenantContext.setTenantId(6L);
        when(customerRepository.countByBranchId(6L)).thenReturn(10L);

        long count = customerService.countAllCustomers();

        assertThat(count).isEqualTo(10L);
        verify(customerRepository).countByBranchId(6L);
    }
}
