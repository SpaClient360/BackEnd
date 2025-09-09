package com.htttql.crmmodule.core.entity;

import com.htttql.crmmodule.common.config.SchemaConstants;
import com.htttql.crmmodule.common.entity.BaseEntity;
import com.htttql.crmmodule.common.enums.TierCode;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Entity
@Table(name = "tier", schema = SchemaConstants.CORE_SCHEMA, indexes = {
        @Index(name = "idx_tier_code", columnList = "code", unique = true),
        @Index(name = "idx_tier_min_spent", columnList = "min_spent"),
        @Index(name = "idx_tier_min_points", columnList = "min_points")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tier extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "tier_seq")
    @SequenceGenerator(name = "tier_seq", sequenceName = SchemaConstants.CORE_SCHEMA + ".tier_seq", allocationSize = 1)
    @Column(name = "tier_id")
    private Long tierId;

    @Enumerated(EnumType.STRING)
    @Column(name = "code", nullable = false, unique = true, length = 20)
    private TierCode code;

    @Column(name = "min_spent", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minSpent = BigDecimal.ZERO;

    @Column(name = "min_points", nullable = false)
    @Builder.Default
    private Integer minPoints = 0;

    @Type(JsonType.class)
    @Column(name = "benefits", columnDefinition = "jsonb")
    private Map<String, Object> benefits;

    @OneToMany(mappedBy = "tier", fetch = FetchType.LAZY)
    @Builder.Default
    private Set<Customer> customers = new HashSet<>();
}
