package com.medicalstore.controller.api;

import com.medicalstore.model.Medicine;
import com.medicalstore.service.MedicineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
}
