package com.htttql.crmmodule.billing.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.PaymentMethod;
import com.htttql.crmmodule.core.entity.StaffUser;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payment", schema = SchemaConstants.BILLING_SCHEMA, indexes = {
        @Index(name = "idx_payment_invoice", columnList = "invoice_id"),
        @Index(name = "idx_payment_method", columnList = "method"),
        @Index(name = "idx_payment_paid_by", columnList = "paid_by"),
        @Index(name = "idx_payment_paid_at", columnList = "paid_at DESC"),
        @Index(name = "idx_payment_txn_ref", columnList = "txn_ref")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "payment_seq")
    @SequenceGenerator(name = "payment_seq", sequenceName = SchemaConstants.BILLING_SCHEMA
            + ".payment_seq", allocationSize = 1)
    @Column(name = "payment_id")
    private Long paymentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "invoice_id", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_invoice"))
    private Invoice invoice;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", nullable = false, length = 20)
    private PaymentMethod method;

    @Column(name = "amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(name = "txn_ref", length = 100)
    private String txnRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paid_by", nullable = false, foreignKey = @ForeignKey(name = "fk_payment_staff"))
    private StaffUser paidBy;

    @Column(name = "paid_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime paidAt;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "card_last_four", length = 4)
    private String cardLastFour;

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "ewallet_provider", length = 50)
    private String ewalletProvider;

    @Column(name = "receipt_number", unique = true, length = 50)
    private String receiptNumber;

    @Column(name = "is_refunded")
    @Builder.Default
    private Boolean isRefunded = false;

    @Column(name = "refunded_at", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime refundedAt;

    @Column(name = "refund_reason", columnDefinition = "TEXT")
    private String refundReason;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (paidAt == null) {
            paidAt = LocalDateTime.now();
        }
        if (receiptNumber == null) {
            // Generate receipt number format: RCP-YYYYMMDD-XXXXX
            receiptNumber = String.format("RCP-%tF-%05d",
                    LocalDateTime.now(),
                    System.currentTimeMillis() % 100000);
        }
    }

    @PreUpdate
    private void validatePayment() {
        if (amount != null && amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }

        // Validate method-specific fields
        if (method == PaymentMethod.CARD && cardLastFour != null && cardLastFour.length() != 4) {
            throw new IllegalArgumentException("Card last four digits must be exactly 4 digits");
        }
    }
}
