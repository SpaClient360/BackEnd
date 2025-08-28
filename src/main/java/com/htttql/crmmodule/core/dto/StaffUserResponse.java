package com.htttql.crmmodule.core.dto;

import com.htttql.crmmodule.common.enums.Gender;
import com.htttql.crmmodule.common.enums.StaffStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffUserResponse {

    private Long staffId;
    private String fullName;
    private String phone;
    private String email;
    private String role;
    private StaffStatus status;
    private LocalDateTime lastLoginAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
