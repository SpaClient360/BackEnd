package com.htttql.crmmodule.core.dto;

import com.htttql.crmmodule.common.enums.TierCode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for Tier Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TierRequest {

    @NotNull(message = "Code is required")
    private TierCode code;

    @NotBlank(message = "Name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Minimum points is required")
    private Integer minPoints;

    @NotNull(message = "Discount rate is required")
    private BigDecimal discountRate;
}
