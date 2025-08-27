package com.htttql.crmmodule.lead.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Create lead response DTO with customer information
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateLeadResponse {

    private Long leadId;
    private String fullName;
    private String phone;
    private String note;
    private String status;
    private boolean isExistingCustomer;
    private String message;

    // Thông tin khách hàng nếu đã tồn tại
    private Long customerId;
    private String tierCode;
    private String tierName;
}
