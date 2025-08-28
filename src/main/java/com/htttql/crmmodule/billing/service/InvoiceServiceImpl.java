package com.htttql.crmmodule.billing.service;

import com.htttql.crmmodule.billing.dto.InvoiceRequest;
import com.htttql.crmmodule.billing.dto.InvoiceResponse;
import com.htttql.crmmodule.billing.dto.InvoiceStatusRequest;
import com.htttql.crmmodule.billing.entity.Invoice;
import com.htttql.crmmodule.billing.repository.IInvoiceRepository;
import com.htttql.crmmodule.common.enums.InvoiceStatus;
import com.htttql.crmmodule.common.exception.BadRequestException;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import com.htttql.crmmodule.core.entity.Customer;
import com.htttql.crmmodule.core.repository.ICustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service("invoiceService")
@RequiredArgsConstructor
public class InvoiceServiceImpl implements IInvoiceService {

    private final IInvoiceRepository invoiceRepository;
    private final ICustomerRepository customerRepository;
    private final ModelMapper modelMapper;

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
                .grandTotal(request.getTotalAmount())
                .status(request.getStatus() != null ? request.getStatus() : InvoiceStatus.DRAFT)
                .notes(request.getNotes())
                .dueDate(request.getDueDate())
                .build();

        invoice = invoiceRepository.save(invoice);
        log.info("Created new invoice: {} for customer: {}", invoice.getInvoiceId(), customer.getCustomerId());
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
            invoice.setGrandTotal(request.getTotalAmount());
        }
        if (request.getNotes() != null)
            invoice.setNotes(request.getNotes());
        if (request.getDueDate() != null)
            invoice.setDueDate(request.getDueDate());

        invoice = invoiceRepository.save(invoice);
        log.info("Updated invoice: {}", invoice.getInvoiceId());
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
        log.info("Deleted invoice: {}", id);
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
        }

        if (request.getNotes() != null) {
            String existingNotes = invoice.getNotes() != null ? invoice.getNotes() + "\n" : "";
            invoice.setNotes(existingNotes + "[Status Update] " + request.getNotes());
        }

        invoice = invoiceRepository.save(invoice);
        log.info("Updated invoice status: {} to {}", invoice.getInvoiceId(), request.getStatus());
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
        InvoiceResponse response = modelMapper.map(invoice, InvoiceResponse.class);
        response.setCustomerId(invoice.getCustomer().getCustomerId());
        response.setCustomerName(invoice.getCustomer().getFullName());
        return response;
    }
}
