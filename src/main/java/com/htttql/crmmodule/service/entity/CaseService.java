package com.htttql.crmmodule.service.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.CaseServiceStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Case service entity for services performed in a customer case
 * Tracks pricing, quantity, and status of each service
 */
@Entity
@Table(name = "case_service", schema = SchemaConstants.SERVICE_SCHEMA, indexes = {
        @Index(name = "idx_case_service_case", columnList = "case_id"),
        @Index(name = "idx_case_service_service", columnList = "service_id"),
        @Index(name = "idx_case_service_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CaseService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "case_service_seq")
    @SequenceGenerator(name = "case_service_seq", sequenceName = SchemaConstants.SERVICE_SCHEMA
            + ".case_service_seq", allocationSize = 1)
    @Column(name = "case_service_id")
    private Long caseServiceId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false, foreignKey = @ForeignKey(name = "fk_case_service_case"))
    private CustomerCase customerCase;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false, foreignKey = @ForeignKey(name = "fk_case_service_service"))
    private SpaService service;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "qty", nullable = false)
    @Builder.Default
    private Integer qty = 1;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CaseServiceStatus status = CaseServiceStatus.PLANNED;

    // Additional fields
    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "discount_reason", length = 200)
    private String discountReason;

    @PrePersist
    @PreUpdate
    private void calculateLineTotal() {
        if (unitPrice != null && qty != null) {
            BigDecimal subtotal = unitPrice.multiply(BigDecimal.valueOf(qty));
            BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
            BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;

            lineTotal = subtotal.subtract(discount).add(tax);

            if (lineTotal.compareTo(BigDecimal.ZERO) < 0) {
                lineTotal = BigDecimal.ZERO;
            }
        }
    }
}
