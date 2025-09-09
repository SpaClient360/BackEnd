package com.htttql.crmmodule.billing.service;

import com.htttql.crmmodule.billing.dto.InvoiceRequest;
import com.htttql.crmmodule.billing.dto.InvoiceResponse;
import com.htttql.crmmodule.billing.dto.InvoiceStatusRequest;
import com.htttql.crmmodule.billing.entity.Invoice;
import com.htttql.crmmodule.billing.entity.PointTransaction;
import com.htttql.crmmodule.billing.repository.IInvoiceRepository;
import com.htttql.crmmodule.billing.repository.IPointTransactionRepository;
import com.htttql.crmmodule.common.enums.InvoiceStatus;
import com.htttql.crmmodule.common.enums.PointTransactionType;
import com.htttql.crmmodule.common.exception.BadRequestException;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.core.entity.Customer;
import com.htttql.crmmodule.core.entity.Tier;
import com.htttql.crmmodule.core.repository.ICustomerRepository;
import com.htttql.crmmodule.core.repository.ITierRepository;
import com.htttql.crmmodule.core.service.ICustomerTierService;
import com.htttql.crmmodule.common.enums.TierCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service("invoiceService")
@RequiredArgsConstructor
public class InvoiceServiceImpl implements IInvoiceService {

    private final IInvoiceRepository invoiceRepository;
    private final ICustomerRepository customerRepository;
    private final IPointTransactionRepository pointTransactionRepository;
    private final ITierRepository tierRepository;
    private final ICustomerTierService customerTierService;

    @Override
    @Transactional(readOnly = true)
    public Page<InvoiceResponse> getAllInvoices(Pageable pageable) {
        Page<Invoice> invoices = invoiceRepository.findAll(pageable);
        return invoices.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public InvoiceResponse getInvoiceById(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));
        return toResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse createInvoice(InvoiceRequest request) {
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));

        String invoiceNumber = generateInvoiceNumber();

        Invoice invoice = Invoice.builder()
                .invoiceNumber(invoiceNumber)
                .customer(customer)
                .subtotal(request.getTotalAmount())
                .status(request.getStatus() != null ? request.getStatus() : InvoiceStatus.DRAFT)
                .notes(request.getNotes())
                .dueDate(request.getDueDate())
                .build();

        invoice = invoiceRepository.save(invoice);

        // Update customer's total spent
        updateCustomerTotalSpent(customer, invoice.getGrandTotal());

        // Award points to customer for successful invoice creation
        awardPointsForInvoice(customer, invoice);

        // Update customer tier based on new spending and points
        customerTierService.refreshCustomerTierInternal(customer.getCustomerId());

        return toResponse(invoice);
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoice(Long id, InvoiceRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        if (request.getCustomerId() != null) {
            Customer customer = customerRepository.findById(request.getCustomerId())
                    .orElseThrow(() -> new ResourceNotFoundException("Customer", "id", request.getCustomerId()));
            invoice.setCustomer(customer);
        }
        if (request.getTotalAmount() != null) {
            invoice.setSubtotal(request.getTotalAmount());
            // Let the entity calculate grandTotal automatically via @PreUpdate
        }
        if (request.getNotes() != null)
            invoice.setNotes(request.getNotes());
        if (request.getDueDate() != null)
            invoice.setDueDate(request.getDueDate());

        invoice = invoiceRepository.save(invoice);
        return toResponse(invoice);
    }

    @Override
    @Transactional
    public void deleteInvoice(Long id) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        if (invoice.getStatus() != InvoiceStatus.DRAFT && invoice.getStatus() != InvoiceStatus.UNPAID) {
            throw new BadRequestException("Can only delete invoices with DRAFT or UNPAID status");
        }

        invoiceRepository.deleteById(id);
    }

    @Override
    @Transactional
    public InvoiceResponse updateInvoiceStatus(Long id, InvoiceStatusRequest request) {
        Invoice invoice = invoiceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", id));

        validateStatusTransition(invoice.getStatus(), request.getStatus());
        invoice.setStatus(request.getStatus());

        if (request.getStatus() == InvoiceStatus.PAID) {
            invoice.setPaidAt(LocalDateTime.now());
            // Update customer tier when invoice is marked as paid
            customerTierService.refreshCustomerTierInternal(invoice.getCustomer().getCustomerId());
        }

        if (request.getNotes() != null) {
            String existingNotes = invoice.getNotes() != null ? invoice.getNotes() + "\n" : "";
            invoice.setNotes(existingNotes + "[Status Update] " + request.getNotes());
        }

        invoice = invoiceRepository.save(invoice);
        return toResponse(invoice);
    }

    private void validateStatusTransition(InvoiceStatus currentStatus, InvoiceStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case DRAFT -> newStatus == InvoiceStatus.UNPAID || newStatus == InvoiceStatus.VOID;
            case UNPAID -> newStatus == InvoiceStatus.PAID || newStatus == InvoiceStatus.VOID;
            case PAID -> newStatus == InvoiceStatus.VOID;
            case VOID -> false;
        };

        if (!isValid) {
            throw new BadRequestException(
                    String.format("Invalid status transition from %s to %s", currentStatus, newStatus));
        }
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 8);
    }

    private InvoiceResponse toResponse(Invoice invoice) {
        return InvoiceResponse.builder()
                .invoiceId(invoice.getInvoiceId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .customerId(invoice.getCustomer().getCustomerId())
                .customerName(invoice.getCustomer().getFullName())
                .totalAmount(invoice.getSubtotal())
                .taxAmount(invoice.getTaxTotal())
                .discountAmount(invoice.getDiscountTotal())
                .finalAmount(invoice.getGrandTotal())
                .status(invoice.getStatus())
                .notes(invoice.getNotes())
                .dueDate(invoice.getDueDate())
                .paidDate(invoice.getPaidAt())
                .createdAt(invoice.getCreatedAt())
                .updatedAt(invoice.getUpdatedAt())
                .build();
    }

    private void updateCustomerTotalSpent(Customer customer, BigDecimal amount) {
        try {
            if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal currentSpent = customer.getTotalSpent() != null ? customer.getTotalSpent() : BigDecimal.ZERO;
                BigDecimal newTotalSpent = currentSpent.add(amount);
                customer.setTotalSpent(newTotalSpent);

                customerRepository.save(customer);
            }
        } catch (Exception e) {
            // Handle exception silently
        }
    }

    private void awardPointsForInvoice(Customer customer, Invoice invoice) {
        try {
            if (invoice.getGrandTotal() == null || invoice.getGrandTotal().compareTo(BigDecimal.ZERO) <= 0) {
                return;
            }

            BigDecimal pointsPer10000VND = BigDecimal.valueOf(10000);
            BigDecimal pointsDecimal = invoice.getGrandTotal().divide(pointsPer10000VND, 0, RoundingMode.DOWN);
            int pointsToAward = pointsDecimal.intValue();

            if (pointsToAward > 0) {
                int currentPoints = customer.getTotalPoints() != null ? customer.getTotalPoints() : 0;
                int newPointsBalance = currentPoints + pointsToAward;

                // Create point transaction
                PointTransaction pointTransaction = PointTransaction.builder()
                        .customer(customer)
                        .source(PointTransactionType.EARN)
                        .points(pointsToAward)
                        .relatedInvoice(invoice)
                        .note(String.format("Earned %d points for invoice %s (%,.0f VND)",
                                pointsToAward, invoice.getInvoiceNumber(),
                                invoice.getGrandTotal().doubleValue()))
                        .balanceBefore(currentPoints)
                        .balanceAfter(newPointsBalance)
                        .build();

                pointTransactionRepository.save(pointTransaction);

                // Update customer points
                customer.setTotalPoints(newPointsBalance);
                customerRepository.save(customer);
            }
        } catch (Exception e) {
            // Don't fail the invoice creation if point awarding fails
        }
    }
}
