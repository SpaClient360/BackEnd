package com.htttql.crmmodule.core.controller;

import com.htttql.crmmodule.core.dto.CustomerRequest;
import com.htttql.crmmodule.core.dto.CustomerResponse;
import com.htttql.crmmodule.core.service.ICustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Customer Management", description = "Customer CRUD operations")
@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final ICustomerService customerService;

    @Operation(summary = "Get all customers with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    @GetMapping
    public ResponseEntity<Page<CustomerResponse>> getAllCustomers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "customerId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<CustomerResponse> customers = customerService.getAllCustomers(pageable);
        return ResponseEntity.ok(customers);
    }

    @Operation(summary = "Get customer by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST', 'TECHNICIAN')")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerResponse> getCustomerById(@PathVariable Long id) {
        CustomerResponse customer = customerService.getCustomerById(id);
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Create new customer")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PostMapping
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerRequest request) {
        CustomerResponse customer = customerService.createCustomer(request);
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Update customer")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerResponse> updateCustomer(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request) {
        CustomerResponse customer = customerService.updateCustomer(id, request);
        return ResponseEntity.ok(customer);
    }

    @Operation(summary = "Delete customer")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long id) {
        customerService.deleteCustomer(id);
        return ResponseEntity.noContent().build();
    }
}
