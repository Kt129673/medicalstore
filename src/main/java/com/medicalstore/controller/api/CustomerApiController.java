package com.medicalstore.controller.api;

import com.medicalstore.model.Customer;
import com.medicalstore.service.CustomerService;
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
 * REST API for customer operations — list, detail, search, count.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer management")
public class CustomerApiController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "List all customers")
    public ResponseEntity<List<Customer>> listCustomers() {
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<Customer> getCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        return customerService.getCustomerById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/count")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Count total customers")
    public ResponseEntity<Map<String, Long>> countCustomers() {
        return ResponseEntity.ok(Map.of("count", customerService.countAllCustomers()));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Search customers by name")
    public ResponseEntity<List<Customer>> searchCustomers(
            @Parameter(description = "Name search query") @RequestParam String q) {
        return ResponseEntity.ok(customerService.searchCustomers(q));
    }

    @GetMapping("/by-phone")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Find customer by phone number")
    public ResponseEntity<Customer> findByPhone(
            @Parameter(description = "Phone number") @RequestParam String phone) {
        return customerService.getCustomerByPhone(phone)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Create a new customer")
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        Customer saved = customerService.saveCustomer(customer);
        var location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SHOPKEEPER')")
    @Operation(summary = "Update an existing customer")
    public ResponseEntity<Customer> updateCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long id,
            @Valid @RequestBody Customer customer) {
        return customerService.getCustomerById(id)
                .map(existing -> {
                    customer.setId(id);
                    return ResponseEntity.ok(customerService.saveCustomer(customer));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a customer")
    public ResponseEntity<Void> deleteCustomer(
            @Parameter(description = "Customer ID") @PathVariable Long id) {
        if (customerService.getCustomerById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
