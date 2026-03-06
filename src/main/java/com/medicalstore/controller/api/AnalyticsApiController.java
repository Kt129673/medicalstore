package com.medicalstore.controller.api;

import com.medicalstore.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Map;

/**
 * REST API for analytics data — profit per medicine, dead stock,
 * fast-moving items, and GST summary.
 */
@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Business analytics and insights")
public class AnalyticsApiController {

    private final AnalyticsService analyticsService;

    @GetMapping("/profit-per-medicine")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'SHOPKEEPER')")
    @Operation(summary = "Get profit per medicine", description = "Returns revenue, cost, profit, qty sold, and margin per medicine for the given date range")
    public ResponseEntity<List<Map<String, Object>>> profitPerMedicine(
            @Parameter(description = "Start date-time (ISO format, default: 30 days ago)") @RequestParam(required = false) LocalDateTime start,
            @Parameter(description = "End date-time (ISO format, default: now)") @RequestParam(required = false) LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime s = start != null ? start : now.minusDays(30);
        LocalDateTime e = end != null ? end : now;
        return ResponseEntity.ok(analyticsService.getProfitPerMedicine(s, e));
    }

    @GetMapping("/dead-stock")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'SHOPKEEPER')")
    @Operation(summary = "Get dead stock items", description = "Items with no sales in the given number of days")
    public ResponseEntity<List<Map<String, Object>>> deadStock(
            @Parameter(description = "Days without sales (default 60)") @RequestParam(defaultValue = "60") int days) {
        return ResponseEntity.ok(analyticsService.getDeadStock(days));
    }

    @GetMapping("/fast-moving")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'SHOPKEEPER')")
    @Operation(summary = "Get fast-moving items", description = "Top-selling items by quantity")
    public ResponseEntity<List<Map<String, Object>>> fastMoving(
            @Parameter(description = "Limit (default 10)") @RequestParam(defaultValue = "10") int limit,
            @Parameter(description = "Start date-time (default: 30 days ago)") @RequestParam(required = false) LocalDateTime start,
            @Parameter(description = "End date-time (default: now)") @RequestParam(required = false) LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime s = start != null ? start : now.minusDays(30);
        LocalDateTime e = end != null ? end : now;
        return ResponseEntity.ok(analyticsService.getFastMovingItems(limit, s, e));
    }

    @GetMapping("/gst-summary")
    @PreAuthorize("hasAnyRole('ADMIN', 'OWNER', 'SHOPKEEPER')")
    @Operation(summary = "Get monthly GST summary", description = "GST breakdown by month for the given year")
    public ResponseEntity<List<Map<String, Object>>> gstSummary(
            @Parameter(description = "Year (default: current year)") @RequestParam(required = false) Integer year) {
        int y = year != null ? year : Year.now().getValue();
        return ResponseEntity.ok(analyticsService.getGstMonthlySummary(y));
    }
}
