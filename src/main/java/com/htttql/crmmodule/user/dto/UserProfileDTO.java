package com.htttql.crmmodule.user.dto;

import com.htttql.crmmodule.user.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * User profile DTO for profile endpoint
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfileDTO {

    private Long id;
    private String email;
    private String phone;
    private String fullName;
    private Gender gender;
    private LocalDate dateOfBirth;
    private String position;
    private String address;
    private String avatar; // optional
    private String note;
}