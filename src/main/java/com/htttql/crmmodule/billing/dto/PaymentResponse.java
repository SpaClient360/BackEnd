package com.htttql.crmmodule.billing.dto;

import com.htttql.crmmodule.common.enums.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Payment Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private Long paymentId;
    private Long invoiceId;
    private BigDecimal amount;
    private PaymentMethod method;
    private String transactionId;
    private String status;
    private LocalDateTime paidAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
