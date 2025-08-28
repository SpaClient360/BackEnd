package com.htttql.crmmodule.billing.service;

import com.htttql.crmmodule.billing.dto.PaymentRequest;
import com.htttql.crmmodule.billing.dto.PaymentResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Payment operations
 */
public interface IPaymentService {

    /**
     * Get all payments with pagination
     */
    Page<PaymentResponse> getAllPayments(Pageable pageable);

    /**
     * Get payment by ID
     */
    PaymentResponse getPaymentById(Long id);

    /**
     * Create new payment
     */
    PaymentResponse createPayment(PaymentRequest request);

    /**
     * Update payment
     */
    PaymentResponse updatePayment(Long id, PaymentRequest request);

    /**
     * Delete payment
     */
    void deletePayment(Long id);

    /**
     * Update payment status
     */
    PaymentResponse updatePaymentStatus(Long id, String status);
}
