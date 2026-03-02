package com.medicalstore.controller;

import com.medicalstore.model.Medicine;
import com.medicalstore.repository.UserRepository;
import com.medicalstore.service.*;
import com.medicalstore.util.SecurityUtils;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MedicineController.class)
@DisplayName("MedicineController UI Tests")
class MedicineControllerTest {

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

    // ─── Helper ──────────────────────────────────────────────────────────────

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

    // ─────────────────────────────────────────────────────────────────────────
    // GET /medicines  (list)
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /medicines – renders medicine list page with paginated medicines")
    void listMedicines_asAdmin_returnsListView() throws Exception {
        Page<Medicine> page = new PageImpl<>(List.of(
                buildMedicine(1L, "Paracetamol 500mg", "Analgesic"),
                buildMedicine(2L, "Amoxicillin 250mg", "Antibiotic")));

        given(medicineService.filterMedicines(any(), any(), any(), any(), any())).willReturn(page);
        given(medicineService.getAllCategories()).willReturn(List.of("Analgesic", "Antibiotic"));
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/medicines"))
                .andExpect(status().isOk())
                .andExpect(view().name("medicines/list"))
                .andExpect(model().attributeExists("medicinePage"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    @WithMockUser(roles = "SHOPKEEPER")
    @DisplayName("GET /medicines – accessible to SHOPKEEPER role")
    void listMedicines_asShopkeeper_returnsListView() throws Exception {
        Page<Medicine> emptyPage = Page.empty();
        given(medicineService.filterMedicines(any(), any(), any(), any(), any())).willReturn(emptyPage);
        given(medicineService.getAllCategories()).willReturn(List.of());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/medicines"))
                .andExpect(status().isOk())
                .andExpect(view().name("medicines/list"));
    }

    @Test
    @DisplayName("GET /medicines – returns 401 for unauthenticated user")
    void listMedicines_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/medicines"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /medicines – passes search param to model")
    void listMedicines_withSearchParam_passesSearchToModel() throws Exception {
        Page<Medicine> page = Page.empty();
        given(medicineService.filterMedicines(any(), any(), any(), any(), any())).willReturn(page);
        given(medicineService.getAllCategories()).willReturn(List.of());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/medicines").param("search", "para"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("currentSearch", "para"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /medicines/new
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /medicines/new – renders add medicine form")
    void showAddForm_asAdmin_rendersForm() throws Exception {
        given(medicineService.getAllCategories()).willReturn(List.of("Analgesic"));
        given(supplierService.getAllSuppliers()).willReturn(List.of());
        given(branchService.getAllBranches()).willReturn(List.of());
        given(securityUtils.isAdmin()).willReturn(true);
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/medicines/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("medicines/form"))
                .andExpect(model().attributeExists("medicine"))
                .andExpect(model().attributeExists("categories"))
                .andExpect(model().attributeExists("suppliers"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /medicines/edit/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /medicines/edit/{id} – renders edit form with medicine data")
    void showEditForm_existingMedicine_rendersFormWithData() throws Exception {
        Medicine med = buildMedicine(1L, "Aspirin", "Analgesic");
        given(medicineService.getMedicineById(1L)).willReturn(Optional.of(med));
        given(medicineService.getAllCategories()).willReturn(List.of("Analgesic"));
        given(supplierService.getAllSuppliers()).willReturn(List.of());
        given(branchService.getAllBranches()).willReturn(List.of());
        given(securityUtils.isAdmin()).willReturn(true);
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/medicines/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("medicines/form"))
                .andExpect(model().attribute("medicine", med));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("GET /medicines/edit/{id} – throws 403/error when medicine not found")
    void showEditForm_notFound_returnsError() throws Exception {
        given(medicineService.getMedicineById(999L)).willReturn(Optional.empty());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(get("/medicines/edit/999"))
                .andExpect(status().is4xxClientError());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /medicines/save
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /medicines/save – redirects to list on success")
    void saveMedicine_valid_redirectsToList() throws Exception {
        Medicine saved = buildMedicine(1L, "Paracetamol", "Analgesic");
        given(securityUtils.isAdmin()).willReturn(true);
        given(securityUtils.isShopkeeper()).willReturn(false);
        given(medicineService.saveMedicine(any(Medicine.class))).willReturn(saved);
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/medicines/save")
                        .with(csrf())
                        .param("name", "Paracetamol")
                        .param("category", "Analgesic")
                        .param("price", "10.0")
                        .param("quantity", "100"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/medicines"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /medicines/save – shows error when service throws IllegalArgumentException")
    void saveMedicine_invalidData_showsError() throws Exception {
        given(securityUtils.isAdmin()).willReturn(true);
        given(securityUtils.isShopkeeper()).willReturn(false);
        given(medicineService.saveMedicine(any())).willThrow(new IllegalArgumentException("Medicine name is required"));
        given(medicineService.getAllCategories()).willReturn(List.of());
        given(supplierService.getAllSuppliers()).willReturn(List.of());
        given(branchService.getAllBranches()).willReturn(List.of());
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/medicines/save")
                        .with(csrf())
                        .param("name", "")
                        .param("category", "Analgesic")
                        .param("price", "10.0")
                        .param("quantity", "100"))
                .andExpect(status().isOk())
                .andExpect(view().name("medicines/form"))
                .andExpect(model().attributeExists("error"));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /medicines/delete/{id}
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /medicines/delete/{id} – redirects to list after deletion")
    void deleteMedicine_asAdmin_redirectsToList() throws Exception {
        given(medicineService.getMedicineById(1L)).willReturn(Optional.of(buildMedicine(1L, "X", "Y")));
        given(subscriptionService.getPlanForOwner(any())).willReturn(Optional.empty());

        mockMvc.perform(post("/medicines/delete/1")
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/medicines"));

        verify(medicineService).deleteMedicine(1L);
    }
}
