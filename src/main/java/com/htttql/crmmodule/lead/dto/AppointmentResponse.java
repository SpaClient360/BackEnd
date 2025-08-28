package com.htttql.crmmodule.lead.dto;

import com.htttql.crmmodule.common.enums.AppointmentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Appointment Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentResponse {
    private Long appointmentId;
    private Long customerId;
    private Long serviceId;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private AppointmentStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
