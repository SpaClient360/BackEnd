package com.htttql.crmmodule.service.controller;

import com.htttql.crmmodule.service.dto.CustomerCaseRequest;
import com.htttql.crmmodule.service.dto.CustomerCaseResponse;
import com.htttql.crmmodule.service.service.ICustomerCaseService;
import com.htttql.crmmodule.common.dto.StatusUpdateRequest;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Customer Case Management Controller
 * Manages customer service cases and treatments
 */
@Tag(name = "Customer Case Management", description = "Customer service cases and treatment management")
@RestController
@RequestMapping("/api/customer-cases")
@RequiredArgsConstructor
public class CustomerCaseController {

    private final ICustomerCaseService customerCaseService;

    @Operation(summary = "Get all customer cases with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN', 'RECEPTIONIST')")
    @GetMapping
    public ResponseEntity<Page<CustomerCaseResponse>> getAllCustomerCases(Pageable pageable) {
        Page<CustomerCaseResponse> cases = customerCaseService.getAllCustomerCases(pageable);
        return ResponseEntity.ok(cases);
    }

    @Operation(summary = "Get customer case by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN', 'RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<CustomerCaseResponse> getCustomerCaseById(@PathVariable Long id) {
        CustomerCaseResponse customerCase = customerCaseService.getCustomerCaseById(id);
        return ResponseEntity.ok(customerCase);
    }

    @Operation(summary = "Create new customer case")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PostMapping
    public ResponseEntity<CustomerCaseResponse> createCustomerCase(@Valid @RequestBody CustomerCaseRequest request) {
        CustomerCaseResponse customerCase = customerCaseService.createCustomerCase(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(customerCase);
    }

    @Operation(summary = "Update customer case")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN')")
    @PutMapping("/{id}")
    public ResponseEntity<CustomerCaseResponse> updateCustomerCase(
            @PathVariable Long id,
            @Valid @RequestBody CustomerCaseRequest request) {
        CustomerCaseResponse customerCase = customerCaseService.updateCustomerCase(id, request);
        return ResponseEntity.ok(customerCase);
    }

    @Operation(summary = "Delete customer case")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCustomerCase(@PathVariable Long id) {
        customerCaseService.deleteCustomerCase(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update customer case status")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN')")
    @PutMapping("/{id}/status")
    public ResponseEntity<CustomerCaseResponse> updateCustomerCaseStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        CustomerCaseResponse customerCase = customerCaseService.updateCustomerCaseStatus(id, request.getStatus());
        return ResponseEntity.ok(customerCase);
    }
}
