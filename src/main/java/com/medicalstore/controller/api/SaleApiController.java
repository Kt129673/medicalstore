package com.medicalstore.controller.api;

import com.medicalstore.dto.SaleRequestDTO;
import com.medicalstore.model.Sale;
import com.medicalstore.model.SaleItem;
import com.medicalstore.model.Medicine;
import com.medicalstore.model.Customer;
import com.medicalstore.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for sales data — list, detail, recent, and today's sales.
 */
@RestController
@RequestMapping("/api/v1/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "Sales transaction management")
public class SaleApiController {

    private final SaleService saleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "List sales (paginated)", description = "Returns paginated sales ordered by most recent first")
    public ResponseEntity<Page<Sale>> listSales(
            @Parameter(description = "Page number (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Page size") @RequestParam(defaultValue = "20") int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "saleDate"));
        return ResponseEntity.ok(saleService.getSalesPaginated(pageRequest));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get sale by ID")
    public ResponseEntity<Sale> getSale(
            @Parameter(description = "Sale ID") @PathVariable Long id) {
        return saleService.getSaleById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get today's total sales amount")
    public ResponseEntity<Map<String, Object>> todaySales() {
        return ResponseEntity.ok(Map.of("todaySalesTotal", saleService.getTodaySales()));
    }

    @GetMapping("/recent")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get recent sales", description = "Returns the most recent sales (limited set)")
    public ResponseEntity<List<Sale>> recentSales() {
        return ResponseEntity.ok(saleService.getRecentSales());
    }

    @GetMapping("/by-customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get sales by customer ID")
    public ResponseEntity<List<Sale>> salesByCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long customerId) {
        return ResponseEntity.ok(saleService.getSalesByCustomer(customerId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Create a new sale", description = "Creates a sale, deducts stock, and awards loyalty points")
    public ResponseEntity<Sale> createSale(@Valid @RequestBody SaleRequestDTO dto) {
        Sale sale = new Sale();

        if (dto.getCustomerId() != null) {
            Customer customer = new Customer();
            customer.setId(dto.getCustomerId());
            sale.setCustomer(customer);
        }

        sale.setPaymentMethod(dto.getPaymentMethod());
        sale.setDiscountPercentage(dto.getDiscountPercentage() != null ? dto.getDiscountPercentage() : 0.0);
        sale.setGstPercentage(dto.getGstPercentage() != null ? dto.getGstPercentage() : 0.0);

        List<SaleItem> items = dto.getItems().stream().map(itemDto -> {
            SaleItem item = new SaleItem();
            Medicine medicine = new Medicine();
            medicine.setId(itemDto.getMedicineId());
            item.setMedicine(medicine);
            item.setQuantity(itemDto.getQuantity());
            item.setUnitPrice(itemDto.getUnitPrice());
            return item;
        }).collect(Collectors.toList());

        sale.setItems(items);

        Sale saved = saleService.createSale(sale);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }
}
