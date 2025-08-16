package com.htttql.crmmodule.user.dto;

import com.htttql.crmmodule.user.enums.Gender;
import com.htttql.crmmodule.user.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Data Transfer Object for User (includes both login and staff profile info)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserDTO {

    private Long id;

    // Login information
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @Size(max = 20, message = "Phone must not exceed 20 characters")
    private String phone;

    private Long roleId;
    private UserStatus status;

    // Staff profile information
    @NotBlank(message = "Full name is required")
    @Size(max = 100, message = "Full name must not exceed 100 characters")
    private String fullName;

    private Gender gender;
    private LocalDate dateOfBirth;

    @Size(max = 50, message = "Position must not exceed 50 characters")
    private String position;

    @Size(max = 255, message = "Address must not exceed 255 characters")
    private String address;

    private String note;

    // System timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}