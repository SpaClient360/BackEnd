package com.htttql.crmmodule.billing.dto;

import com.htttql.crmmodule.common.enums.InvoiceStatus;
import com.htttql.crmmodule.common.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

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
    private PaymentMethod paymentMethod;
    private InvoiceStatus status;
    private String notes;
    private LocalDateTime dueDate;
    private LocalDateTime paidDate;
    private List<InvoiceItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class InvoiceItemResponse {

    private Long itemId;
    private Long serviceId;
    private String serviceName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal totalPrice;
}
