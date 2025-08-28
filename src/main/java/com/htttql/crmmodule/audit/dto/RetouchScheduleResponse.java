package com.htttql.crmmodule.audit.dto;

import com.htttql.crmmodule.common.enums.RetouchStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Retouch Schedule Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RetouchScheduleResponse {
    private Long scheduleId;
    private Long customerId;
    private Long caseId;
    private LocalDate scheduledDate;
    private RetouchStatus status;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
