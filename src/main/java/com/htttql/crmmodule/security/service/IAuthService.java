package com.htttql.crmmodule.security.service;

import com.htttql.crmmodule.security.dto.AuthRequest;
import com.htttql.crmmodule.security.dto.JwtResponse;
import com.htttql.crmmodule.security.dto.OtpRequest;
import com.htttql.crmmodule.security.dto.OtpVerifyRequest;
import com.htttql.crmmodule.security.dto.RefreshTokenRequest;
import com.htttql.crmmodule.security.dto.ChangePasswordRequest;

/**
 * Interface for Authentication service operations
 */
public interface IAuthService {

    /**
     * Authenticate user and return JWT token
     */
    JwtResponse authenticate(AuthRequest request);

    /**
     * Refresh JWT token
     */
    JwtResponse refreshToken(RefreshTokenRequest request);

    /**
     * Send OTP for verification
     */
    void sendOtp(OtpRequest request);

    /**
     * Verify OTP
     */
    JwtResponse verifyOtp(OtpVerifyRequest request);

    /**
     * Change password
     */
    void changePassword(ChangePasswordRequest request);

    /**
     * Logout user
     */
    void logout(String token);

    /**
     * Validate JWT token
     */
    boolean validateToken(String token);
}
