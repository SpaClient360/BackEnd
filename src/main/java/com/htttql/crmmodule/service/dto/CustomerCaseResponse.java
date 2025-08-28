package com.htttql.crmmodule.service.dto;

import com.htttql.crmmodule.common.enums.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for Customer Case Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCaseResponse {
    private Long caseId;
    private Long customerId;
    private Long serviceId;
    private CaseStatus status;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
