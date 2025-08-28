package com.htttql.crmmodule.core.dto;

import com.htttql.crmmodule.common.enums.TierCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for Tier Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierResponse {
    private Long tierId;
    private TierCode code;
    private String name;
    private String description;
    private Integer minPoints;
    private BigDecimal discountRate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
