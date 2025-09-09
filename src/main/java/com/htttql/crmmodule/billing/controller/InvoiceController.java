package com.htttql.crmmodule.billing.controller;

import com.htttql.crmmodule.billing.dto.InvoiceRequest;
import com.htttql.crmmodule.billing.dto.InvoiceResponse;
import com.htttql.crmmodule.billing.dto.InvoiceStatusRequest;
import com.htttql.crmmodule.billing.service.IInvoiceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Invoice Management", description = "Invoice CRUD operations")
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final IInvoiceService invoiceService;

    @Operation(summary = "Get all invoices with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping
    public ResponseEntity<Page<InvoiceResponse>> getAllInvoices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "invoiceId") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<InvoiceResponse> invoices = invoiceService.getAllInvoices(pageable);
        return ResponseEntity.ok(invoices);
    }

    @Operation(summary = "Get invoice by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<InvoiceResponse> getInvoiceById(@PathVariable Long id) {
        InvoiceResponse invoice = invoiceService.getInvoiceById(id);
        return ResponseEntity.ok(invoice);
    }

    @Operation(summary = "Create new invoice")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PostMapping
    public ResponseEntity<InvoiceResponse> createInvoice(@Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse invoice = invoiceService.createInvoice(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(invoice);
    }

    @Operation(summary = "Update invoice")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<InvoiceResponse> updateInvoice(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceRequest request) {
        InvoiceResponse invoice = invoiceService.updateInvoice(id, request);
        return ResponseEntity.ok(invoice);
    }

    @Operation(summary = "Delete invoice")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteInvoice(@PathVariable Long id) {
        invoiceService.deleteInvoice(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update invoice status")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PutMapping("/{id}/status")
    public ResponseEntity<InvoiceResponse> updateInvoiceStatus(
            @PathVariable Long id,
            @Valid @RequestBody InvoiceStatusRequest request) {
        InvoiceResponse invoice = invoiceService.updateInvoiceStatus(id, request);
        return ResponseEntity.ok(invoice);
    }
}
