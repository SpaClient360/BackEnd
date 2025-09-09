package com.htttql.crmmodule.billing.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.InvoiceStatus;
import com.htttql.crmmodule.core.entity.Customer;
import com.htttql.crmmodule.service.entity.CustomerCase;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "invoice", schema = SchemaConstants.BILLING_SCHEMA, indexes = {
        @Index(name = "idx_invoice_case", columnList = "case_id"),
        @Index(name = "idx_invoice_customer", columnList = "customer_id"),
        @Index(name = "idx_invoice_status", columnList = "status"),
        @Index(name = "idx_invoice_created", columnList = "created_at DESC"),
        @Index(name = "idx_invoice_paid_at", columnList = "paid_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Invoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "invoice_seq")
    @SequenceGenerator(name = "invoice_seq", sequenceName = SchemaConstants.BILLING_SCHEMA
            + ".invoice_seq", allocationSize = 1)
    @Column(name = "invoice_id")
    private Long invoiceId;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", unique = true, foreignKey = @ForeignKey(name = "fk_invoice_case"))
    private CustomerCase customerCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false, foreignKey = @ForeignKey(name = "fk_invoice_customer"))
    private Customer customer;

    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "discount_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountTotal = BigDecimal.ZERO;

    @Column(name = "tax_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxTotal = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal grandTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.DRAFT;

    @Column(name = "paid_at", columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime paidAt;

    // Invoice details
    @Column(name = "invoice_number", unique = true, length = 50)
    private String invoiceNumber;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "promo_code", length = 50)
    private String promoCode;

    @Column(name = "promo_discount", precision = 12, scale = 2)
    private BigDecimal promoDiscount;

    @Column(name = "points_redeemed")
    @Builder.Default
    private Integer pointsRedeemed = 0;

    @Column(name = "points_value", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal pointsValue = BigDecimal.ZERO;

    @OneToMany(mappedBy = "invoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Payment> payments = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        super.onCreate();
        if (invoiceNumber == null) {
            // Generate invoice number format: INV-YYYYMMDD-XXXXX
            invoiceNumber = String.format("INV-%tF-%05d",
                    LocalDateTime.now(),
                    System.currentTimeMillis() % 100000);
        }
        calculateGrandTotal();
    }

    @PreUpdate
    protected void onUpdate() {
        super.onUpdate();
        calculateGrandTotal();
    }

    private void calculateGrandTotal() {
        if (subtotal != null) {
            BigDecimal discount = discountTotal != null ? discountTotal : BigDecimal.ZERO;
            BigDecimal tax = taxTotal != null ? taxTotal : BigDecimal.ZERO;
            BigDecimal points = pointsValue != null ? pointsValue : BigDecimal.ZERO;

            // Calculate: subtotal - discount + tax - pointsValue
            grandTotal = subtotal.subtract(discount).add(tax).subtract(points);

            // Ensure grandTotal is not negative
            if (grandTotal.compareTo(BigDecimal.ZERO) < 0) {
                // If pointsValue causes negative total, reduce pointsValue instead
                BigDecimal maxPointsValue = subtotal.subtract(discount).add(tax);
                if (maxPointsValue.compareTo(BigDecimal.ZERO) > 0) {
                    pointsValue = maxPointsValue;
                    grandTotal = BigDecimal.ZERO;
                } else {
                    grandTotal = BigDecimal.ZERO;
                }
            }
        }
    }

    public BigDecimal getTotalPaid() {
        return payments.stream()
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getBalanceDue() {
        return grandTotal.subtract(getTotalPaid());
    }
}
