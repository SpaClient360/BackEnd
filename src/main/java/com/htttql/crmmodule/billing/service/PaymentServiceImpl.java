package com.htttql.crmmodule.billing.service;

import com.htttql.crmmodule.billing.dto.PaymentRequest;
import com.htttql.crmmodule.billing.dto.PaymentResponse;
import com.htttql.crmmodule.billing.entity.Payment;
import com.htttql.crmmodule.common.enums.PaymentMethod;
import com.htttql.crmmodule.billing.repository.IPaymentRepository;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of Payment Service
 */
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements IPaymentService {

    private final IPaymentRepository paymentRepository;

    @Override
    public Page<PaymentResponse> getAllPayments(Pageable pageable) {
        Page<Payment> payments = paymentRepository.findAll(pageable);
        return payments.map(this::mapToResponse);
    }

    @Override
    public PaymentResponse getPaymentById(Long id) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
        return mapToResponse(payment);
    }

    @Override
    public PaymentResponse createPayment(PaymentRequest request) {
        // Note: This is a simplified implementation
        // In real scenario, you would need to fetch Invoice and StaffUser entities
        Payment payment = Payment.builder()
                .amount(request.getAmount())
                .method(request.getMethod())
                .txnRef(request.getTransactionId())
                .paidAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);
        return mapToResponse(savedPayment);
    }

    @Override
    public PaymentResponse updatePayment(Long id, PaymentRequest request) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        payment.setAmount(request.getAmount());
        payment.setMethod(request.getMethod());
        payment.setTxnRef(request.getTransactionId());

        Payment updatedPayment = paymentRepository.save(payment);
        return mapToResponse(updatedPayment);
    }

    @Override
    public void deletePayment(Long id) {
        if (!paymentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Payment not found with id: " + id);
        }
        paymentRepository.deleteById(id);
    }

    @Override
    public PaymentResponse updatePaymentStatus(Long id, String status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));

        // Note: Payment entity doesn't have status field
        // This is a simplified implementation
        if ("COMPLETED".equals(status)) {
            payment.setPaidAt(LocalDateTime.now());
        }

        Payment updatedPayment = paymentRepository.save(payment);
        return mapToResponse(updatedPayment);
    }

    private PaymentResponse mapToResponse(Payment payment) {
        return PaymentResponse.builder()
                .paymentId(payment.getPaymentId())
                .invoiceId(payment.getInvoice() != null ? payment.getInvoice().getInvoiceId() : null)
                .amount(payment.getAmount())
                .method(payment.getMethod())
                .transactionId(payment.getTxnRef())
                .status("COMPLETED") // Simplified - Payment entity doesn't have status
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}
