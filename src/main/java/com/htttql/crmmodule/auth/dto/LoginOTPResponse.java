package com.htttql.crmmodule.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for OTP login request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginOTPResponse {

    private String message;
    private String error;
    private Long remainingMinutes;
    private String email;

    /**
     * Create success response
     */
    public static LoginOTPResponse success(String message) {
        return LoginOTPResponse.builder()
                .message(message)
                .build();
    }

    /**
     * Create success response with remaining time
     */
    public static LoginOTPResponse success(String message, Long remainingMinutes) {
        return LoginOTPResponse.builder()
                .message(message)
                .remainingMinutes(remainingMinutes)
                .build();
    }

    /**
     * Create success response with email and remaining time
     */
    public static LoginOTPResponse success(String message, Long remainingMinutes, String email) {
        return LoginOTPResponse.builder()
                .message(message)
                .remainingMinutes(remainingMinutes)
                .email(email)
                .build();
    }

    /**
     * Create error response
     */
    public static LoginOTPResponse error(String error) {
        return LoginOTPResponse.builder()
                .error(error)
                .build();
    }
}