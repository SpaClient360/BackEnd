package com.htttql.crmmodule.audit.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for Retouch Schedule Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetouchScheduleRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotNull(message = "Scheduled date is required")
    private LocalDate scheduledDate;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
