package com.htttql.crmmodule.security.controller;

import com.htttql.crmmodule.common.dto.ApiResponse;
import com.htttql.crmmodule.security.dto.*;
import com.htttql.crmmodule.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Authentication REST controller
 */
@Tag(name = "Authentication", description = "Authentication endpoints")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Request OTP for login (Public API)")
    @PostMapping("/otp/request")
    public ResponseEntity<ApiResponse<String>> requestOtp(@Valid @RequestBody OtpRequest request) {
        String result = authService.requestOtp(request);
        return ResponseEntity.ok(ApiResponse.success(result, "OTP sent successfully"));
    }

    @Operation(summary = "Verify OTP and login (Public API)")
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<JwtResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request) {
        JwtResponse response = authService.verifyOtpAndLogin(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @Operation(summary = "Login with password (Public API)")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<JwtResponse>> login(@Valid @RequestBody AuthRequest request) {
        JwtResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

    @Operation(summary = "Refresh access token (Public API)")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<JwtResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        JwtResponse response = authService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Token refreshed successfully"));
    }

    @Operation(summary = "Logout")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String token) {
        authService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Logout successful"));
    }

    @Operation(summary = "Change password")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String token) {
        authService.changePassword(request, token);
        return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
    }
}
