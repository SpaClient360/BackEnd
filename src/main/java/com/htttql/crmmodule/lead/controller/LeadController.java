package com.htttql.crmmodule.lead.controller;

import com.htttql.crmmodule.common.enums.LeadStatus;
import com.htttql.crmmodule.lead.dto.LeadRequest;
import com.htttql.crmmodule.lead.dto.LeadResponse;
import com.htttql.crmmodule.lead.dto.LeadStatusRequest;
import com.htttql.crmmodule.lead.dto.LeadStats;
import com.htttql.crmmodule.lead.service.ILeadService;
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

@Tag(name = "Lead Management", description = "Lead CRUD operations")
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
public class LeadController {

    private final ILeadService leadService;

    @Operation(summary = "Get all leads with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping
    public ResponseEntity<Page<LeadResponse>> getAllLeads(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "leadId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LeadResponse> leads = leadService.getAllLeads(pageable);
        return ResponseEntity.ok(leads);
    }

    @Operation(summary = "Get lead by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<LeadResponse> getLeadById(@PathVariable Long id) {
        LeadResponse lead = leadService.getLeadById(id);
        return ResponseEntity.ok(lead);
    }

    @Operation(summary = "Create new lead (Public API - No authentication required)")
    @PostMapping
    public ResponseEntity<LeadResponse> createLead(@Valid @RequestBody LeadRequest request) {
        LeadResponse lead = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(lead);
    }

    @Operation(summary = "Update lead")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PutMapping("/{id}")
    public ResponseEntity<LeadResponse> updateLead(
            @PathVariable Long id,
            @Valid @RequestBody LeadRequest request) {
        LeadResponse lead = leadService.updateLead(id, request);
        return ResponseEntity.ok(lead);
    }

    @Operation(summary = "Delete lead")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteLead(@PathVariable Long id) {
        leadService.deleteLead(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update lead status")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PutMapping("/{id}/status")
    public ResponseEntity<LeadResponse> updateLeadStatus(
            @PathVariable Long id,
            @Valid @RequestBody LeadStatusRequest request) {
        LeadResponse lead = leadService.updateLeadStatus(id, request);
        return ResponseEntity.ok(lead);
    }

    @Operation(summary = "Get lead statistics")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/stats")
    public ResponseEntity<LeadStats> getLeadStats() {
        LeadStats stats = leadService.getLeadStats();
        return ResponseEntity.ok(stats);
    }

    @Operation(summary = "Get leads by status")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<LeadResponse>> getLeadsByStatus(
            @PathVariable LeadStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "leadId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<LeadResponse> leads = leadService.getLeadsByStatus(status, pageable);
        return ResponseEntity.ok(leads);
    }
}