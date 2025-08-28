package com.htttql.crmmodule.service.dto;

import com.htttql.crmmodule.common.enums.ServiceCategory;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRequest {

    @NotBlank(message = "Service code is required")
    @Size(min = 2, max = 50, message = "Service code must be between 2 and 50 characters")
    private String code;

    @NotBlank(message = "Service name is required")
    @Size(min = 2, max = 200, message = "Service name must be between 2 and 200 characters")
    private String name;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Category is required")
    private ServiceCategory category;

    @NotNull(message = "Price is required")
    @Positive(message = "Price must be positive")
    private BigDecimal price;

    @Positive(message = "Duration must be positive")
    private Integer durationMinutes;

    private Boolean isActive;
}
