package com.htttql.crmmodule.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * OTP request DTO for requesting OTP
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OtpRequest {

    @NotBlank(message = "Username is required")
    @Pattern(regexp = "^([0-9]{10,11}|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})$", message = "Username must be a valid phone number or email")
    private String username; // Phone or Email

    @NotBlank(message = "Password is required")
    private String password;

    @Builder.Default
    private OtpPurpose purpose = OtpPurpose.LOGIN;

    public enum OtpPurpose {
        LOGIN,
        RESET_PASSWORD,
        VERIFY_ACCOUNT,
        CHANGE_PHONE,
        CHANGE_EMAIL
    }
}
