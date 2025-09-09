package com.htttql.crmmodule.billing.dto;

import com.htttql.crmmodule.common.enums.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private Long invoiceId;
    private String invoiceNumber;
    private Long customerId;
    private String customerName;
    private BigDecimal totalAmount;
    private BigDecimal taxAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private InvoiceStatus status;
    private String notes;
    private LocalDateTime dueDate;
    private LocalDateTime paidDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
