package com.htttql.crmmodule.auth.controller;

import com.htttql.crmmodule.auth.dto.AuthResponse;
import com.htttql.crmmodule.auth.dto.ErrorResponse;
import com.htttql.crmmodule.auth.dto.LoginRequest;
import com.htttql.crmmodule.auth.dto.LoginOTPResponse;
import com.htttql.crmmodule.auth.dto.LogoutRequest;
import com.htttql.crmmodule.auth.dto.MeResponse;
import com.htttql.crmmodule.auth.dto.RefreshTokenRequest;
import com.htttql.crmmodule.auth.dto.RefreshTokenResponse;
import com.htttql.crmmodule.auth.dto.RegisterRequest;
import com.htttql.crmmodule.auth.dto.VerifyOTPRequest;
import com.htttql.crmmodule.auth.service.AuthService;
import com.htttql.crmmodule.user.dto.UserDTO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication REST Controller
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthService authService;

    /**
     * User login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .accessToken(null)
                            .build());
        }
    }

    /**
     * User registration
     */
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(AuthResponse.builder()
                            .accessToken(null)
                            .build());
        }
    }

    /**
     * Login with OTP - Step 1: Verify credentials and send OTP via email
     * 
     * @param request login request containing email/phone and password
     * @return LoginOTPResponse indicating success or failure
     */
    @PostMapping("/login-otp")
    public ResponseEntity<LoginOTPResponse> loginWithOTP(@Valid @RequestBody LoginRequest request) {
        try {
            LoginOTPResponse response = authService.loginWithOTP(request);

            if (response.getError() != null) {
                // Log failed attempt (without sensitive info)
                log.warn("OTP login failed: {}", response.getError());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }

            // Log successful OTP generation
            log.info("OTP sent successfully for login request");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Unexpected error during OTP login: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginOTPResponse.error("Có lỗi xảy ra. Vui lòng thử lại sau."));
        }
    }

    /**
     * Verify OTP and complete login - Step 2: Verify OTP and get JWT token
     * 
     * @param request OTP verification request
     * @return AuthResponse with JWT token if verification successful
     */
    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOTPAndLogin(@Valid @RequestBody VerifyOTPRequest request) {
        try {
            AuthResponse response = authService.verifyOTPAndLogin(request);
            log.info("OTP verification successful");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.warn("OTP verification failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(AuthResponse.builder()
                            .accessToken(null)
                            .build());
        }
    }

    /**
     * Refresh access token using refresh token
     * 
     * @param request refresh token request
     * @return RefreshTokenResponse with new access and refresh tokens
     */
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        try {
            RefreshTokenResponse response = authService.refreshAccessToken(request);
            log.info("Token refreshed successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.warn("Token refresh failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of("Invalid or expired refresh token", "Please login again"));
        }
    }

    /**
     * Logout with refresh token revocation
     * 
     * @param request logout request with refresh token
     * @return success message
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@Valid @RequestBody LogoutRequest request) {
        try {
            authService.logoutWithRefreshToken(request.getRefreshToken());

            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");

            log.info("User logged out successfully");
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            // Always return success for logout to prevent information disclosure
            Map<String, String> response = new HashMap<>();
            response.put("message", "Logged out successfully");
            return ResponseEntity.ok(response);
        }
    }

    /**
     * Get current user information
     * 
     * @return current user details
     */
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.getName() != null) {
                UserDTO userDTO = authService.getCurrentUser(authentication.getName());
                return ResponseEntity.ok(MeResponse.of(userDTO));
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of("Unauthorized", "Invalid or expired token"));

        } catch (Exception e) {
            log.error("Error getting current user: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ErrorResponse.of("Unauthorized", "Invalid or expired token"));
        }
    }

    /**
     * Health check for auth service
     */
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Auth service is running");
    }

}