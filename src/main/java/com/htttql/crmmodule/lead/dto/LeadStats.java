package com.htttql.crmmodule.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for lead statistics
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadStats {

    private Integer todayCount;
    private Integer totalCount;
    private String lastUpdated;
}
