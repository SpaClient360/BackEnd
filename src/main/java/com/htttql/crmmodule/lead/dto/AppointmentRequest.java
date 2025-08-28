package com.htttql.crmmodule.lead.dto;

import com.htttql.crmmodule.common.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Appointment Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentRequest {

    @NotNull(message = "Customer ID is required")
    private Long customerId;

    @NotNull(message = "Service ID is required")
    private Long serviceId;

    @NotNull(message = "Start time is required")
    private LocalDateTime startAt;

    @NotNull(message = "End time is required")
    private LocalDateTime endAt;

    @NotNull(message = "Status is required")
    private AppointmentStatus status;

    @Size(max = 500, message = "Notes must not exceed 500 characters")
    private String notes;
}
