package com.htttql.crmmodule.billing.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.PromotionType;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Entity
@Table(name = "promotion", schema = SchemaConstants.BILLING_SCHEMA, indexes = {
        @Index(name = "idx_promo_code", columnList = "code", unique = true),
        @Index(name = "idx_promo_active", columnList = "is_active"),
        @Index(name = "idx_promo_dates", columnList = "start_at, end_at"),
        @Index(name = "idx_promo_type", columnList = "type")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Promotion extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "promo_seq")
    @SequenceGenerator(name = "promo_seq", sequenceName = SchemaConstants.BILLING_SCHEMA
            + ".promotion_seq", allocationSize = 1)
    @Column(name = "promo_id")
    private Long promoId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 20)
    private PromotionType type;

    @Column(name = "value", nullable = false, precision = 12, scale = 2)
    private BigDecimal value;

    @Column(name = "start_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime startAt;

    @Column(name = "end_at", nullable = false, columnDefinition = "TIMESTAMPTZ")
    private LocalDateTime endAt;

    @Type(JsonType.class)
    @Column(name = "conditions", columnDefinition = "jsonb")
    private Map<String, Object> conditions;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "min_purchase_amount", precision = 12, scale = 2)
    private BigDecimal minPurchaseAmount;

    @Column(name = "max_discount_amount", precision = 12, scale = 2)
    private BigDecimal maxDiscountAmount;

    @Column(name = "usage_limit")
    private Integer usageLimit;

    @Column(name = "usage_count")
    @Builder.Default
    private Integer usageCount = 0;

    @Column(name = "usage_limit_per_customer")
    private Integer usageLimitPerCustomer;

    @Column(name = "applicable_services", columnDefinition = "TEXT")
    private String applicableServices;

    @Column(name = "applicable_tiers", columnDefinition = "TEXT")
    private String applicableTiers;

    @Column(name = "is_combinable")
    @Builder.Default
    private Boolean isCombinable = false;

    @PrePersist
    @PreUpdate
    private void validatePromotion() {
        if (code != null) {
            code = code.toUpperCase().trim();
        }

        if (value != null && value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Promotion value must be positive");
        }

        if (type == PromotionType.PERCENT && value.compareTo(new BigDecimal("100")) > 0) {
            throw new IllegalArgumentException("Percentage discount cannot exceed 100%");
        }

        if (startAt != null && endAt != null && endAt.isBefore(startAt)) {
            throw new IllegalArgumentException("End date must be after start date");
        }

        if (usageLimit != null && usageCount != null && usageCount > usageLimit) {
            isActive = false;
        }
    }

    public boolean isValid() {
        LocalDateTime now = LocalDateTime.now();
        return isActive &&
                now.isAfter(startAt) &&
                now.isBefore(endAt) &&
                (usageLimit == null || usageCount < usageLimit);
    }

    public BigDecimal calculateDiscount(BigDecimal amount) {
        if (!isValid() || amount == null) {
            return BigDecimal.ZERO;
        }

        if (minPurchaseAmount != null && amount.compareTo(minPurchaseAmount) < 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal discount;
        if (type == PromotionType.PERCENT) {
            discount = amount.multiply(value).divide(new BigDecimal("100"));
        } else {
            discount = value;
        }

        if (maxDiscountAmount != null && discount.compareTo(maxDiscountAmount) > 0) {
            discount = maxDiscountAmount;
        }

        return discount;
    }
}
