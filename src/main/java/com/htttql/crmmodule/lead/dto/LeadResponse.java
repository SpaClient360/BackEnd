package com.htttql.crmmodule.lead.dto;

import com.htttql.crmmodule.common.enums.LeadStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeadResponse {

    private Long leadId;
    private String fullName;
    private String phone;
    private String note;
    private LeadStatus status;
    private String ipAddress;
    private String userAgent;
    private Long customerId;
    private Boolean isExistingCustomer;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
