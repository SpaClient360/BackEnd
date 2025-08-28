package com.htttql.crmmodule.service.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.ServiceCategory;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Service entity for spa services
 * Categories: LIP, BROW, OTHER
 */
@Entity
@Table(name = "service", schema = SchemaConstants.SERVICE_SCHEMA, indexes = {
        @Index(name = "idx_service_code", columnList = "code", unique = true),
        @Index(name = "idx_service_category", columnList = "category"),
        @Index(name = "idx_service_active", columnList = "is_active"),
        @Index(name = "idx_service_name", columnList = "name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SpaService extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "service_seq")
    @SequenceGenerator(name = "service_seq", sequenceName = SchemaConstants.SERVICE_SCHEMA
            + ".service_seq", allocationSize = 1)
    @Column(name = "service_id")
    private Long serviceId;

    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private ServiceCategory category;

    @Column(name = "base_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(name = "duration_min", nullable = false)
    private Integer durationMin;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    // Additional service configuration
    @Column(name = "requires_consultation")
    @Builder.Default
    private Boolean requiresConsultation = false;

    @Column(name = "requires_patch_test")
    @Builder.Default
    private Boolean requiresPatchTest = false;

    @Column(name = "retouch_days")
    private Integer retouchDays;

    @Column(name = "warranty_days")
    private Integer warrantyDays;

    @PrePersist
    @PreUpdate
    private void validateService() {
        if (code != null) {
            code = code.toUpperCase().trim();
        }
        if (basePrice != null && basePrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Base price cannot be negative");
        }
        if (durationMin != null && durationMin <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
    }
}
