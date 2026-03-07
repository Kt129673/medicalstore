package com.medicalstore.service;

import com.medicalstore.common.TenantContext;
import com.medicalstore.model.Medicine;
import com.medicalstore.repository.MedicineRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MedicineService Unit Tests")
class MedicineServiceTest {

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private com.medicalstore.kafka.EventPublisher eventPublisher;

    @InjectMocks
    private MedicineService medicineService;

    // Ensure TenantContext is cleared before each test so global branch is used
    @BeforeEach
    void clearTenantContext() {
        TenantContext.clear();
    }

    // ─── Helper ──────────────────────────────────────────────────────────────

    private Medicine buildMedicine(Long id, String name, String category, double price, int qty) {
        Medicine m = new Medicine();
        m.setId(id);
        m.setName(name);
        m.setCategory(category);
        m.setPrice(price);
        m.setQuantity(qty);
        m.setExpiryDate(LocalDate.now().plusMonths(6));
        return m;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllMedicines
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllMedicines – returns all medicines when no tenant context")
    void getAllMedicines_noContext_returnsAll() {
        List<Medicine> expected = List.of(
                buildMedicine(1L, "Paracetamol", "Analgesic", 10.0, 100),
                buildMedicine(2L, "Amoxicillin", "Antibiotic", 25.0, 50));
        when(medicineRepository.findAll()).thenReturn(expected);

        List<Medicine> result = medicineService.getAllMedicines();

        assertThat(result).hasSize(2).containsExactlyElementsOf(expected);
        verify(medicineRepository).findAll();
    }

    @Test
    @DisplayName("getAllMedicines – filters by branch when tenant context is set")
    void getAllMedicines_withTenantContext_returnsBranchMedicines() {
        TenantContext.setTenantId(5L);
        List<Medicine> branchMeds = List.of(buildMedicine(1L, "Ibuprofen", "Analgesic", 12.0, 80));
        when(medicineRepository.findByBranchId(5L)).thenReturn(branchMeds);

        List<Medicine> result = medicineService.getAllMedicines();

        assertThat(result).hasSize(1);
        verify(medicineRepository).findByBranchId(5L);
        verify(medicineRepository, never()).findAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getMedicineById
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getMedicineById – returns medicine when found and no tenant restriction")
    void getMedicineById_found_returnsOptional() {
        Medicine m = buildMedicine(1L, "Aspirin", "Analgesic", 8.0, 200);
        when(medicineRepository.findById(1L)).thenReturn(Optional.of(m));

        Optional<Medicine> result = medicineService.getMedicineById(1L);

        assertThat(result).isPresent().contains(m);
    }

    @Test
    @DisplayName("getMedicineById – returns empty when medicine not found")
    void getMedicineById_notFound_returnsEmpty() {
        when(medicineRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Medicine> result = medicineService.getMedicineById(99L);

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // saveMedicine – validation
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("saveMedicine – saves valid medicine and auto-assigns barcode")
    void saveMedicine_valid_savesAndAutoBarcode() {
        Medicine m = buildMedicine(null, "Cetirizine", "Antihistamine", 5.0, 50);
        m.setBarcode(null); // no barcode supplied
        Medicine saved = buildMedicine(10L, "Cetirizine", "Antihistamine", 5.0, 50);
        when(medicineRepository.save(any(Medicine.class))).thenReturn(saved);

        Medicine result = medicineService.saveMedicine(m);

        assertThat(result.getId()).isEqualTo(10L);
        // barcode was auto-assigned (starts with "BAR-")
        assertThat(m.getBarcode()).startsWith("BAR-");
        verify(medicineRepository).save(m);
    }

    @Test
    @DisplayName("saveMedicine – throws IllegalArgumentException when name is blank")
    void saveMedicine_blankName_throwsException() {
        Medicine m = buildMedicine(null, "  ", "Analgesic", 10.0, 10);

        assertThatThrownBy(() -> medicineService.saveMedicine(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("name is required");
    }

    @Test
    @DisplayName("saveMedicine – throws IllegalArgumentException when category is blank")
    void saveMedicine_blankCategory_throwsException() {
        Medicine m = buildMedicine(null, "Aspirin", "", 10.0, 10);

        assertThatThrownBy(() -> medicineService.saveMedicine(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Category is required");
    }

    @Test
    @DisplayName("saveMedicine – throws IllegalArgumentException when price is zero")
    void saveMedicine_zeroPrice_throwsException() {
        Medicine m = buildMedicine(null, "Aspirin", "Analgesic", 0.0, 10);

        assertThatThrownBy(() -> medicineService.saveMedicine(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("price is required");
    }

    @Test
    @DisplayName("saveMedicine – throws IllegalArgumentException when quantity is negative")
    void saveMedicine_negativeQuantity_throwsException() {
        Medicine m = buildMedicine(null, "Aspirin", "Analgesic", 10.0, -1);

        assertThatThrownBy(() -> medicineService.saveMedicine(m))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("quantity is required");
    }

    @Test
    @DisplayName("saveMedicine – preserves supplied barcode without overwriting")
    void saveMedicine_withBarcode_preservesIt() {
        Medicine m = buildMedicine(null, "Vitamin C", "Supplement", 15.0, 100);
        m.setBarcode("CUSTOM-001");
        when(medicineRepository.save(m)).thenReturn(m);

        medicineService.saveMedicine(m);

        assertThat(m.getBarcode()).isEqualTo("CUSTOM-001");
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteMedicine
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteMedicine – delegates to repository")
    void deleteMedicine_delegatesToRepository() {
        medicineService.deleteMedicine(7L);

        verify(medicineRepository).deleteById(7L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // countAllMedicines
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("countAllMedicines – returns global count when no context")
    void countAllMedicines_noContext_returnsGlobalCount() {
        when(medicineRepository.count()).thenReturn(42L);

        long count = medicineService.countAllMedicines();

        assertThat(count).isEqualTo(42L);
        verify(medicineRepository).count();
    }

    @Test
    @DisplayName("countAllMedicines – returns branch count when tenant context is set")
    void countAllMedicines_withTenantContext_returnsBranchCount() {
        TenantContext.setTenantId(3L);
        when(medicineRepository.countByBranchId(3L)).thenReturn(15L);

        long count = medicineService.countAllMedicines();

        assertThat(count).isEqualTo(15L);
        verify(medicineRepository).countByBranchId(3L);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getLowStockMedicines
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getLowStockMedicines – returns medicines below threshold globally")
    void getLowStockMedicines_global_returnsMedicinesBelowThreshold() {
        List<Medicine> low = List.of(buildMedicine(1L, "TestMed", "Cat", 10.0, 3));
        when(medicineRepository.findByQuantityLessThan(10)).thenReturn(low);

        List<Medicine> result = medicineService.getLowStockMedicines(10);

        assertThat(result).hasSize(1);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getExpiredMedicines
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getExpiredMedicines – calls repository with today's date")
    void getExpiredMedicines_callsRepoWithToday() {
        when(medicineRepository.findByExpiryDateBefore(any(LocalDate.class))).thenReturn(List.of());

        medicineService.getExpiredMedicines();

        verify(medicineRepository).findByExpiryDateBefore(LocalDate.now());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getExpiringSoonMedicines
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getExpiringSoonMedicines – calls repository with correct date range")
    void getExpiringSoonMedicines_callsRepoWithDateRange() {
        when(medicineRepository.findByExpiryDateBetween(any(), any())).thenReturn(List.of());

        medicineService.getExpiringSoonMedicines(30);

        verify(medicineRepository).findByExpiryDateBetween(
                LocalDate.now(), LocalDate.now().plusDays(30));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // searchMedicines
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("searchMedicines – delegates to global search when no tenant context")
    void searchMedicines_noContext_searchesGlobally() {
        List<Medicine> found = List.of(buildMedicine(1L, "Paracetamol 500mg", "Analgesic", 10.0, 50));
        when(medicineRepository.findByNameContainingIgnoreCase("para")).thenReturn(found);

        List<Medicine> result = medicineService.searchMedicines("para");

        assertThat(result).hasSize(1);
        verify(medicineRepository).findByNameContainingIgnoreCase("para");
    }
}
