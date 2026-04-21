package com.medicalstore.controller.api;

import com.medicalstore.model.Supplier;
import com.medicalstore.service.SupplierService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

/**
 * REST API for supplier operations — list, detail, search.
 */
@RestController
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
@Tag(name = "Suppliers", description = "Supplier management")
public class SupplierApiController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "List all suppliers")
    public ResponseEntity<List<Supplier>> listSuppliers() {
        return ResponseEntity.ok(supplierService.getAllSuppliers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get supplier by ID")
    public ResponseEntity<Supplier> getSupplier(
            @Parameter(description = "Supplier ID") @PathVariable Long id) {
        return supplierService.getSupplierById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Search suppliers by name")
    public ResponseEntity<List<Supplier>> searchSuppliers(
            @Parameter(description = "Name search query") @RequestParam String q) {
        return ResponseEntity.ok(supplierService.searchSuppliers(q));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Create a new supplier")
    public ResponseEntity<Supplier> createSupplier(@Valid @RequestBody Supplier supplier) {
        Supplier saved = supplierService.saveSupplier(supplier);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Update an existing supplier")
    public ResponseEntity<Supplier> updateSupplier(
            @Parameter(description = "Supplier ID") @PathVariable Long id,
            @Valid @RequestBody Supplier supplier) {
        return supplierService.getSupplierById(id)
                .map(existing -> {
                    supplier.setId(id);
                    return ResponseEntity.ok(supplierService.saveSupplier(supplier));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a supplier")
    public ResponseEntity<Void> deleteSupplier(
            @Parameter(description = "Supplier ID") @PathVariable Long id) {
        if (supplierService.getSupplierById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        supplierService.deleteSupplier(id);
        return ResponseEntity.noContent().build();
    }
}
