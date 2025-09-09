package com.htttql.crmmodule.core.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "customer", schema = SchemaConstants.CORE_SCHEMA, indexes = {
        @Index(name = "idx_customer_phone", columnList = "phone", unique = true),
        @Index(name = "idx_customer_email", columnList = "email"),
        @Index(name = "idx_customer_tier", columnList = "tier_id"),
        @Index(name = "idx_customer_total_spent", columnList = "total_spent"),
        @Index(name = "idx_customer_total_points", columnList = "total_points")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "customer_seq")
    @SequenceGenerator(name = "customer_seq", sequenceName = SchemaConstants.CORE_SCHEMA
            + ".customer_seq", allocationSize = 1)
    @Column(name = "customer_id")
    private Long customerId;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "phone", nullable = false, unique = true, length = 20)
    private String phone;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "dob")
    private LocalDate dob;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 20)
    private Gender gender;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tier_id", nullable = false, foreignKey = @ForeignKey(name = "fk_customer_tier"))
    private Tier tier;

    @Column(name = "total_points", nullable = false)
    @Builder.Default
    private Integer totalPoints = 0;

    @Column(name = "total_spent", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalSpent = BigDecimal.ZERO;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "is_vip")
    @Builder.Default
    private Boolean isVip = false;

    @PrePersist
    @PreUpdate
    private void normalizeData() {
        if (phone != null) {
            phone = phone.replaceAll("[^0-9]", "");
        }
        if (email != null) {
            email = email.toLowerCase().trim();
        }
        if (fullName != null) {
            fullName = fullName.trim();
        }
    }
}
