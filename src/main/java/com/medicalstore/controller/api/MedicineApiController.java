package com.medicalstore.controller.api;

import com.medicalstore.model.Medicine;
import com.medicalstore.service.MedicineService;
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
import java.util.Map;

/**
 * REST API for medicine inventory operations.
 * Exposes CRUD + search + stock alert endpoints.
 */
@RestController
@RequestMapping("/api/v1/medicines")
@RequiredArgsConstructor
@Tag(name = "Medicines", description = "Medicine inventory management")
public class MedicineApiController {

    private final MedicineService medicineService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "List all medicines", description = "Returns all medicines visible to the current tenant/branch context")
    public ResponseEntity<List<Medicine>> listMedicines() {
        return ResponseEntity.ok(medicineService.getAllMedicines());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get medicine by ID")
    public ResponseEntity<Medicine> getMedicine(
            @Parameter(description = "Medicine ID") @PathVariable Long id) {
        return medicineService.getMedicineById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Count total medicines")
    public ResponseEntity<Map<String, Long>> countMedicines() {
        return ResponseEntity.ok(Map.of("count", medicineService.countAllMedicines()));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get low stock medicines", description = "Returns medicines with quantity below the given threshold")
    public ResponseEntity<List<Medicine>> lowStock(
            @Parameter(description = "Stock threshold (default 10)") @RequestParam(defaultValue = "10") Integer threshold) {
        return ResponseEntity.ok(medicineService.getLowStockMedicines(threshold));
    }

    @GetMapping("/expiring-soon")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get medicines expiring soon", description = "Returns medicines expiring within the given number of days")
    public ResponseEntity<List<Medicine>> expiringSoon(
            @Parameter(description = "Days until expiry (default 30)") @RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(medicineService.getExpiringSoonMedicines(days));
    }

    @GetMapping("/expired")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get expired medicines")
    public ResponseEntity<List<Medicine>> expired() {
        return ResponseEntity.ok(medicineService.getExpiredMedicines());
    }

    @GetMapping("/categories")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get all unique medicine categories")
    public ResponseEntity<List<String>> categories() {
        return ResponseEntity.ok(medicineService.getAllCategories());
    }

    @GetMapping("/by-category")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get medicines by category")
    public ResponseEntity<List<Medicine>> byCategory(
            @Parameter(description = "Category name") @RequestParam String category) {
        return ResponseEntity.ok(medicineService.getMedicinesByCategory(category));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Search medicines for POS")
    public ResponseEntity<List<com.medicalstore.dto.MedicineDTO>> searchMedicines(
            @RequestParam(name = "q", defaultValue = "") String query) {

        if (query.isBlank()) {
            return ResponseEntity.ok(List.of());
        }

        List<com.medicalstore.dto.MedicineDTO> results = medicineService.searchMedicinesForPos(query);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Create a new medicine")
    public ResponseEntity<Medicine> createMedicine(@Valid @RequestBody Medicine medicine) {
        Medicine saved = medicineService.saveMedicine(medicine);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Update an existing medicine")
    public ResponseEntity<Medicine> updateMedicine(
            @Parameter(description = "Medicine ID") @PathVariable Long id,
            @Valid @RequestBody Medicine medicine) {
        return medicineService.getMedicineById(id)
                .map(existing -> {
                    medicine.setId(id);
                    return ResponseEntity.ok(medicineService.saveMedicine(medicine));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a medicine")
    public ResponseEntity<Void> deleteMedicine(
            @Parameter(description = "Medicine ID") @PathVariable Long id) {
        if (medicineService.getMedicineById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        medicineService.deleteMedicine(id);
        return ResponseEntity.noContent().build();
    }
}
