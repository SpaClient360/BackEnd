package com.htttql.crmmodule.billing.controller;

import com.htttql.crmmodule.billing.dto.PaymentRequest;
import com.htttql.crmmodule.billing.dto.PaymentResponse;
import com.htttql.crmmodule.billing.service.IPaymentService;
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
 * Payment Management Controller
 * Manages payment operations and transactions
 */
@Tag(name = "Payment Management", description = "Payment operations and transaction management")
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final IPaymentService paymentService;

    @Operation(summary = "Get all payments with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping
    public ResponseEntity<Page<PaymentResponse>> getAllPayments(Pageable pageable) {
        Page<PaymentResponse> payments = paymentService.getAllPayments(pageable);
        return ResponseEntity.ok(payments);
    }

    @Operation(summary = "Get payment by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Long id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @Operation(summary = "Create new payment")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        PaymentResponse payment = paymentService.createPayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(payment);
    }

    @Operation(summary = "Update payment")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @PutMapping("/{id}")
    public ResponseEntity<PaymentResponse> updatePayment(
            @PathVariable Long id,
            @Valid @RequestBody PaymentRequest request) {
        PaymentResponse payment = paymentService.updatePayment(id, request);
        return ResponseEntity.ok(payment);
    }

    @Operation(summary = "Delete payment")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Long id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update payment status")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'RECEPTIONIST')")
    @PutMapping("/{id}/status")
    public ResponseEntity<PaymentResponse> updatePaymentStatus(
            @PathVariable Long id,
            @Valid @RequestBody StatusUpdateRequest request) {
        PaymentResponse payment = paymentService.updatePaymentStatus(id, request.getStatus());
        return ResponseEntity.ok(payment);
    }
}
