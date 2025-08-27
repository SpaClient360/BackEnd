package com.htttql.crmmodule.lead.dto;

import com.htttql.crmmodule.common.enums.LeadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Update lead status request DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateLeadStatusRequest {

    @NotNull(message = "Status is required")
    private LeadStatus status;

    private String note;
}
