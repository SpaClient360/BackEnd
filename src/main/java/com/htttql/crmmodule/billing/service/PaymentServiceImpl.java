package com.htttql.crmmodule.billing.service;

import com.htttql.crmmodule.billing.dto.PaymentRequest;
import com.htttql.crmmodule.billing.dto.PaymentResponse;
import com.htttql.crmmodule.billing.entity.Payment;
import com.htttql.crmmodule.billing.repository.IPaymentRepository;
import com.htttql.crmmodule.billing.repository.IInvoiceRepository;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.core.entity.Customer;
import com.htttql.crmmodule.core.entity.Tier;
import com.htttql.crmmodule.core.repository.ICustomerRepository;
import com.htttql.crmmodule.core.repository.ITierRepository;
import com.htttql.crmmodule.common.enums.TierCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentServiceImpl implements IPaymentService {

    private final IPaymentRepository paymentRepository;
    private final IInvoiceRepository invoiceRepository;
    private final ICustomerRepository customerRepository;
    private final ITierRepository tierRepository;

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
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // Fetch and validate invoice
        var invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("Invoice not found with id: " + request.getInvoiceId()));

        // Validate payment amount doesn't exceed invoice total
        BigDecimal totalPaid = invoice.getTotalPaid();
        BigDecimal remainingAmount = invoice.getGrandTotal().subtract(totalPaid);
        if (request.getAmount().compareTo(remainingAmount) > 0) {
            throw new IllegalArgumentException("Payment amount exceeds remaining invoice amount: " + remainingAmount);
        }

        // Create payment linked to invoice
        Payment payment = Payment.builder()
                .invoice(invoice)
                .amount(request.getAmount())
                .method(request.getMethod())
                .txnRef(request.getTransactionId())
                .paidAt(LocalDateTime.now())
                .build();

        Payment savedPayment = paymentRepository.save(payment);

        updateCustomerTierAfterPayment(invoice.getCustomer(), request.getAmount());

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
                .status("COMPLETED")
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }

    private void updateCustomerTierAfterPayment(Customer customer, BigDecimal paymentAmount) {
        try {

            BigDecimal totalSpent = customer.getTotalSpent() != null ? customer.getTotalSpent() : BigDecimal.ZERO;
            Integer totalPoints = customer.getTotalPoints() != null ? customer.getTotalPoints() : 0;

            TierCode newTierCode = determineTierCode(totalSpent, totalPoints);
            Tier currentTier = customer.getTier();

            // Only update if tier has changed
            if (currentTier == null || !currentTier.getCode().equals(newTierCode)) {
                Tier newTier = tierRepository.findByCode(newTierCode)
                        .orElseThrow(() -> new RuntimeException("Tier not found: " + newTierCode));

                customer.setTier(newTier);

                // Update VIP flag based on tier
                boolean isVip = newTierCode == TierCode.VIP;
                customer.setIsVip(isVip);

                customerRepository.save(customer);
            }
        } catch (Exception e) {
        }
    }

    private TierCode determineTierCode(BigDecimal totalSpent, Integer totalPoints) {
        if (totalSpent.compareTo(BigDecimal.valueOf(TierCode.VIP.getMinSpent())) >= 0 &&
                totalPoints >= TierCode.VIP.getMinPoints()) {
            return TierCode.VIP;
        }

        if (totalSpent.compareTo(BigDecimal.valueOf(TierCode.GOLD.getMinSpent())) >= 0 &&
                totalPoints >= TierCode.GOLD.getMinPoints()) {
            return TierCode.GOLD;
        }

        if (totalSpent.compareTo(BigDecimal.valueOf(TierCode.SILVER.getMinSpent())) >= 0 &&
                totalPoints >= TierCode.SILVER.getMinPoints()) {
            return TierCode.SILVER;
        }

        return TierCode.REGULAR;
    }
}
