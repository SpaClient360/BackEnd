package com.htttql.crmmodule.core.dto;

import com.htttql.crmmodule.common.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerResponse {

    private Long customerId;
    private String fullName;
    private String phone;
    private String email;
    private LocalDate dob;
    private Gender gender;
    private String address;
    private String notes;
    private Boolean isVip;
    private String tierCode;
    private String tierName;
    private Integer totalPoints;
    private BigDecimal totalSpent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
