package com.htttql.crmmodule.lead.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.htttql.crmmodule.common.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lead DTO - Simplified version
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class LeadDto {

    private Long leadId;

    private String fullName;

    private String phone;

    private String note;

    @Builder.Default
    private LeadStatus status = LeadStatus.NEW;

    private String ipAddress;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
