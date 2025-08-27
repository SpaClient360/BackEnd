package com.htttql.crmmodule.lead.controller;

import com.htttql.crmmodule.common.dto.ApiResponse;
import com.htttql.crmmodule.lead.dto.CreateLeadRequest;
import com.htttql.crmmodule.lead.dto.CreateLeadResponse;
import com.htttql.crmmodule.lead.dto.LeadDto;
import com.htttql.crmmodule.lead.dto.UpdateLeadStatusRequest;
import com.htttql.crmmodule.lead.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Lead management controller
 */
@Tag(name = "Lead Management", description = "Lead CRUD operations")
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final LeadService leadService;

    @Operation(summary = "Get all leads with pagination", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER')")
    public ResponseEntity<ApiResponse<Page<LeadDto>>> getAllLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        Page<LeadDto> leads = leadService.getAllLeads(page, size);
        return ResponseEntity.ok(ApiResponse.success(leads));
    }

    @Operation(summary = "Get lead by ID", security = @SecurityRequirement(name = "Bearer Authentication"))
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER')")
    public ResponseEntity<ApiResponse<LeadDto>> getLeadById(@PathVariable Long id) {
        LeadDto lead = leadService.getLeadById(id);
        return ResponseEntity.ok(ApiResponse.success(lead));
    }

    @Operation(summary = "Create new lead (Public API - No authentication required)")
    @PostMapping
    public ResponseEntity<ApiResponse<CreateLeadResponse>> createLead(
            @Valid @RequestBody CreateLeadRequest request) {

        CreateLeadResponse response = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, response.getMessage()));
    }

    @Operation(summary = "Update lead status", security = @SecurityRequirement(name = "Bearer Authentication"))
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('RECEPTIONIST', 'MANAGER')")
    public ResponseEntity<ApiResponse<LeadDto>> updateLeadStatus(
            @PathVariable Long id,
            @Valid @RequestBody UpdateLeadStatusRequest request) {

        LeadDto lead = leadService.updateLeadStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(lead, "Lead status updated successfully"));
    }

    @Operation(summary = "Delete lead", security = @SecurityRequirement(name = "Bearer Authentication"))
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('MANAGER')")
    public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Lead deleted successfully"));
    }
}