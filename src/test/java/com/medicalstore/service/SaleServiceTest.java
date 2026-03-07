package com.medicalstore.service;

import com.medicalstore.common.TenantContext;
import com.medicalstore.model.*;
import com.medicalstore.repository.CustomerRepository;
import com.medicalstore.repository.MedicineRepository;
import com.medicalstore.repository.SaleRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleService Unit Tests")
class SaleServiceTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private MedicineRepository medicineRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private com.medicalstore.kafka.EventPublisher eventPublisher;

    @InjectMocks
    private SaleService saleService;

    @BeforeEach
    void clearContext() {
        TenantContext.clear();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private Medicine buildMedicine(Long id, String name, int qty, double price) {
        Medicine m = new Medicine();
        m.setId(id);
        m.setName(name);
        m.setQuantity(qty);
        m.setPrice(price);
        m.setPurchasePrice(price * 0.6);
        return m;
    }

    private SaleItem buildItem(Medicine medicine, int qty, double unitPrice) {
        SaleItem item = new SaleItem();
        item.setMedicine(medicine);
        item.setQuantity(qty);
        item.setUnitPrice(unitPrice);
        item.setCostPrice(0.0);
        item.calculateTotal();
        return item;
    }

    private Sale buildSale(Medicine medicine, int qty, double unitPrice) {
        Sale sale = new Sale();
        sale.setPaymentMethod("Cash");
        sale.setDiscountPercentage(0.0);
        sale.setGstPercentage(0.0);

        SaleItem item = buildItem(medicine, qty, unitPrice);
        sale.addItem(item);
        return sale;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getAllSales
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getAllSales – returns all sales when no tenant context")
    void getAllSales_noContext_returnsAll() {
        List<Sale> expected = List.of(new Sale(), new Sale());
        when(saleRepository.findAll()).thenReturn(expected);

        List<Sale> result = saleService.getAllSales();

        assertThat(result).hasSize(2);
        verify(saleRepository).findAll();
    }

    @Test
    @DisplayName("getAllSales – filters by branch when tenant context is set")
    void getAllSales_withTenantContext_returnsBranchSales() {
        TenantContext.setTenantId(3L);
        List<Sale> branchSales = List.of(new Sale());
        when(saleRepository.findByBranchId(3L)).thenReturn(branchSales);

        List<Sale> result = saleService.getAllSales();

        assertThat(result).hasSize(1);
        verify(saleRepository).findByBranchId(3L);
        verify(saleRepository, never()).findAll();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getSaleById
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getSaleById – returns sale when found and no tenant restriction")
    void getSaleById_found_returnsSale() {
        Sale sale = new Sale();
        sale.setId(1L);
        when(saleRepository.findByIdWithDetails(1L)).thenReturn(Optional.of(sale));

        Optional<Sale> result = saleService.getSaleById(1L);

        assertThat(result).isPresent().contains(sale);
    }

    @Test
    @DisplayName("getSaleById – returns empty when sale not found")
    void getSaleById_notFound_returnsEmpty() {
        when(saleRepository.findByIdWithDetails(99L)).thenReturn(Optional.empty());

        Optional<Sale> result = saleService.getSaleById(99L);

        assertThat(result).isEmpty();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // createSale
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("createSale – throws BusinessException when items list is empty")
    void createSale_noItems_throwsException() {
        Sale sale = new Sale();
        sale.setItems(new ArrayList<>());

        assertThatThrownBy(() -> saleService.createSale(sale))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("at least one item");
    }

    @Test
    @DisplayName("createSale – throws ResourceNotFoundException when medicine not found in DB")
    void createSale_medicineNotFound_throwsException() {
        Medicine ghost = buildMedicine(999L, "Ghost", 10, 50.0);
        Sale sale = buildSale(ghost, 2, 50.0);

        when(medicineRepository.findAllById(List.of(999L))).thenReturn(List.of()); // not found

        assertThatThrownBy(() -> saleService.createSale(sale))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Medicine not found");
    }

    @Test
    @DisplayName("createSale – throws BusinessException when insufficient stock")
    void createSale_insufficientStock_throwsException() {
        Medicine med = buildMedicine(1L, "Aspirin", 5, 10.0); // only 5 in stock
        Sale sale = buildSale(med, 10, 10.0); // try to sell 10

        when(medicineRepository.findAllById(List.of(1L))).thenReturn(List.of(med));
        // Check is done before deductStock—quantity 5 < required 10

        assertThatThrownBy(() -> saleService.createSale(sale))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Insufficient stock");
    }

    @Test
    @DisplayName("createSale – persists sale and deducts stock on success")
    void createSale_success_savesAndDeductsStock() {
        Medicine med = buildMedicine(2L, "Paracetamol", 100, 10.0);
        Sale sale = buildSale(med, 5, 10.0);
        Sale savedSale = new Sale();
        savedSale.setId(1L);

        when(medicineRepository.findAllById(List.of(2L))).thenReturn(List.of(med));
        when(medicineRepository.deductStock(2L, 5)).thenReturn(1); // success
        when(saleRepository.save(any(Sale.class))).thenReturn(savedSale);

        Sale result = saleService.createSale(sale);

        assertThat(result.getId()).isEqualTo(1L);
        verify(saleRepository).save(sale);
        // total amount should be 5 * 10 = 50
        assertThat(sale.getTotalAmount()).isEqualTo(50.0);
    }

    @Test
    @DisplayName("createSale – awards loyalty points to customer on sale")
    void createSale_withCustomer_awardsLoyaltyPoints() {
        Medicine med = buildMedicine(3L, "Vitamin D", 50, 100.0);
        Sale sale = buildSale(med, 2, 100.0); // total = 200 → 2 loyalty points

        Customer customer = new Customer();
        customer.setId(10L);
        customer.setName("Test Customer");
        customer.setLoyaltyPoints(0);
        customer.setPhone("9000000001");
        sale.setCustomer(customer);

        Sale savedSale = new Sale();
        savedSale.setId(5L);

        when(medicineRepository.findAllById(List.of(3L))).thenReturn(List.of(med));
        when(medicineRepository.deductStock(3L, 2)).thenReturn(1);
        when(customerRepository.findById(10L)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenReturn(customer);
        when(saleRepository.save(any(Sale.class))).thenReturn(savedSale);

        saleService.createSale(sale);

        // 200 / 100 = 2 points
        assertThat(customer.getLoyaltyPoints()).isEqualTo(2);
        verify(customerRepository).save(customer);
    }

    @Test
    @DisplayName("createSale – sets finalAmount/discount/gst even when no customer (walk-in sale)")
    void createSale_noCustomer_setsFinalAmountCorrectly() {
        Medicine med = buildMedicine(4L, "Ibuprofen", 50, 100.0);
        Sale sale = buildSale(med, 2, 100.0); // total = 200
        sale.setDiscountPercentage(10.0); // 10% discount → 20 off → 180
        sale.setGstPercentage(5.0); // 5% GST on 180 → 9 → final = 189

        when(medicineRepository.findAllById(List.of(4L))).thenReturn(List.of(med));
        when(medicineRepository.deductStock(4L, 2)).thenReturn(1);
        when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> inv.getArgument(0));

        Sale result = saleService.createSale(sale);

        assertThat(result.getDiscountAmount()).isEqualTo(20.0);
        assertThat(result.getGstAmount()).isEqualTo(9.0);
        assertThat(result.getFinalAmount()).isEqualTo(189.0);
        // No customer interaction
        verify(customerRepository, never()).save(any());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getTodaySales
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getTodaySales – returns 0.0 when repository returns null")
    void getTodaySales_nullResult_returnsZero() {
        when(saleRepository.getTotalSalesBetween(any(), any())).thenReturn(null);

        double result = saleService.getTodaySales();

        assertThat(result).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getTodaySales – returns repository value when not null")
    void getTodaySales_validResult_returnsValue() {
        when(saleRepository.getTotalSalesBetween(any(), any())).thenReturn(500.0);

        double result = saleService.getTodaySales();

        assertThat(result).isEqualTo(500.0);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // deleteSale – restores stock on deletion
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteSale – throws ResourceNotFoundException when sale not found")
    void deleteSale_notFound_throwsException() {
        when(saleRepository.findByIdWithDetails(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> saleService.deleteSale(88L))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Sale not found");
    }

    @Test
    @DisplayName("deleteSale – uses addStock() to restore stock and deletes sale")
    void deleteSale_found_restoresStockAndDeletes() {
        Medicine med = buildMedicine(1L, "Aspirin", 40, 10.0);

        SaleItem item = buildItem(med, 10, 10.0);
        Sale sale = new Sale();
        sale.setId(7L);
        sale.addItem(item);

        when(saleRepository.findByIdWithDetails(7L)).thenReturn(Optional.of(sale));
        when(medicineRepository.addStock(1L, 10)).thenReturn(1);

        saleService.deleteSale(7L);

        verify(medicineRepository).addStock(1L, 10);
        verify(medicineRepository, never()).save(any());
        verify(saleRepository).delete(sale);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // getRecentSales
    // ─────────────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getRecentSales – returns top 5 globally when no context")
    void getRecentSales_noContext_returnsTop5() {
        List<Sale> recent = List.of(new Sale(), new Sale());
        when(saleRepository.findTop5ByOrderBySaleDateDesc()).thenReturn(recent);

        List<Sale> result = saleService.getRecentSales();

        assertThat(result).hasSize(2);
        verify(saleRepository).findTop5ByOrderBySaleDateDesc();
    }
}
