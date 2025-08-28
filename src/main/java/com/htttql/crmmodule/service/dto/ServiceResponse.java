package com.htttql.crmmodule.service.dto;

import com.htttql.crmmodule.common.enums.ServiceCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceResponse {

    private Long serviceId;
    private String code;
    private String name;
    private String description;
    private ServiceCategory category;
    private BigDecimal price;
    private Integer durationMinutes;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
