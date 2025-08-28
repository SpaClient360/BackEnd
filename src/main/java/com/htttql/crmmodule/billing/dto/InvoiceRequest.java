package com.htttql.crmmodule.billing.dto;

import com.htttql.crmmodule.common.enums.InvoiceStatus;
import com.htttql.crmmodule.common.enums.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
public class InvoiceRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    private InvoiceStatus status;
    private String notes;
    private LocalDateTime dueDate;
    private List<InvoiceItemRequest> items;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class InvoiceItemRequest {

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Quantity is required")
    @Positive(message = "Quantity must be positive")
    private Integer quantity;

    @NotNull(message = "Unit price is required")
    @Positive(message = "Unit price must be positive")
    private BigDecimal unitPrice;
}
