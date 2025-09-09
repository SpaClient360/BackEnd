package com.htttql.crmmodule.billing.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.PointTransactionType;
import com.htttql.crmmodule.core.entity.Customer;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "point_transaction", schema = SchemaConstants.BILLING_SCHEMA, indexes = {
        @Index(name = "idx_point_txn_customer", columnList = "customer_id"),
        @Index(name = "idx_point_txn_invoice", columnList = "related_invoice_id"),
        @Index(name = "idx_point_txn_type", columnList = "source"),
        @Index(name = "idx_point_txn_created", columnList = "created_at DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "point_txn_seq")
    @SequenceGenerator(name = "point_txn_seq", sequenceName = SchemaConstants.BILLING_SCHEMA
            + ".point_transaction_seq", allocationSize = 1)
    @Column(name = "point_txn_id")
    private Long pointTxnId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_point_txn_customer"))
    private Customer customer;

    @Enumerated(EnumType.STRING)
    @Column(name = "source", nullable = false, length = 20)
    private PointTransactionType source;

    @Column(name = "points", nullable = false)
    private Integer points;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "related_invoice_id", foreignKey = @ForeignKey(name = "fk_point_txn_invoice"))
    private Invoice relatedInvoice;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @Column(name = "balance_before", nullable = false)
    private Integer balanceBefore;

    @Column(name = "balance_after", nullable = false)
    private Integer balanceAfter;

    @Column(name = "reference_number", unique = true, length = 50)
    private String referenceNumber;

    @Column(name = "expires_at", columnDefinition = "TIMESTAMPTZ")
    private java.time.LocalDateTime expiresAt;

    @Column(name = "is_expired")
    @Builder.Default
    private Boolean isExpired = false;

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (referenceNumber == null) {
            referenceNumber = String.format("PTS-%tF-%05d",
                    java.time.LocalDateTime.now(),
                    System.currentTimeMillis() % 100000);
        }
        if (source == PointTransactionType.EARN && expiresAt == null) {
            expiresAt = java.time.LocalDateTime.now().plusYears(1);
        }
    }

    @PreUpdate
    private void validateTransaction() {
        if (source == PointTransactionType.REDEEM && points > 0) {
            points = -Math.abs(points); // Ensure redemptions are negative
        }

        if (balanceAfter < 0) {
            throw new IllegalArgumentException("Point balance cannot be negative");
        }

        if (balanceBefore + points != balanceAfter) {
            throw new IllegalArgumentException("Point balance calculation mismatch");
        }
    }
}
