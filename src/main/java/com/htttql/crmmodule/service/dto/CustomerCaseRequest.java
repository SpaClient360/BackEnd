package com.htttql.crmmodule.service.dto;

import com.htttql.crmmodule.service.enums.CaseServiceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for Customer Case Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCaseRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Status is required")
    private CaseServiceStatus status;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    private LocalDate endDate;

    @Size(max = 1000, message = "Notes must not exceed 1000 characters")
    private String notes;
}
