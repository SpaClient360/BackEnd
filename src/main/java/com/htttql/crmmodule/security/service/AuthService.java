package com.htttql.crmmodule.security.service;

import com.htttql.crmmodule.common.enums.StaffStatus;
import com.htttql.crmmodule.common.exception.BadRequestException;
import com.htttql.crmmodule.common.exception.UnauthorizedException;
import com.htttql.crmmodule.core.entity.StaffUser;
import com.htttql.crmmodule.core.repository.StaffUserRepository;
import com.htttql.crmmodule.security.dto.*;
import com.htttql.crmmodule.security.jwt.JwtUtils;
import com.htttql.crmmodule.security.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Authentication service
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService implements IAuthService {

    private final AuthenticationManager authenticationManager;
    private final StaffUserRepository staffUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final OtpService otpService;
    private final TokenBlacklistService tokenBlacklistService;

    public String requestOtp(OtpRequest request) {
        // Find user by email or phone
        StaffUser user = staffUserRepository.findByEmailOrPhone(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new BadRequestException("User not found"));

        if (user.getStatus() != StaffStatus.ACTIVE) {
            throw new BadRequestException("User account is not active");
        }

        // Verify password
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Invalid password");
        }

        return otpService.generateAndSendOtp(request.getUsername(), request.getPurpose());
    }

    @Transactional
    public JwtResponse verifyOtpAndLogin(OtpVerifyRequest request) {
        // Verify OTP
        boolean isValid = otpService.verifyOtp(
                request.getUsername(),
                request.getOtpCode(),
                OtpRequest.OtpPurpose.LOGIN);

        if (!isValid) {
            throw new UnauthorizedException("Invalid or expired OTP");
        }

        // Find user
        StaffUser user = staffUserRepository.findByEmailOrPhone(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Create authentication token
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user.getEmail() != null ? user.getEmail() : user.getPhone(),
                null);

        return createJwtResponse(user, authentication);
    }

    @Transactional
    public JwtResponse login(AuthRequest request) {
        // Authenticate user
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Find user
        StaffUser user = staffUserRepository.findByEmailOrPhone(request.getUsername(), request.getUsername())
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        return createJwtResponse(user, authentication);
    }

    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        // Validate refresh token
        if (!jwtUtils.validateToken(refreshToken)) {
            throw new UnauthorizedException("Invalid refresh token");
        }

        // Check if refresh token exists in Redis
        String username = jwtUtils.getUsernameFromToken(refreshToken);
        String storedToken = tokenBlacklistService.getRefreshToken(username);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new UnauthorizedException("Refresh token not found or expired");
        }

        // Find user
        StaffUser user = staffUserRepository.findByEmailOrPhone(username, username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Generate new tokens
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getCode());

        String newAccessToken = jwtUtils.generateToken(username, claims);
        String newRefreshToken = jwtUtils.generateRefreshToken(username);

        // Store new refresh token
        tokenBlacklistService.storeRefreshToken(
                username,
                newRefreshToken,
                Duration.ofMillis(jwtUtils.getRefreshExpirationTime()));

        return buildJwtResponse(user, newAccessToken, newRefreshToken);
    }

    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);

            // Add token to blacklist
            long expiration = jwtUtils.getExpirationDateFromToken(token).getTime() - System.currentTimeMillis();
            tokenBlacklistService.blacklistToken(token, expiration);

            // Remove refresh token
            String username = jwtUtils.getUsernameFromToken(token);
            tokenBlacklistService.removeRefreshToken(username);
        }
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request, String authHeader) {
        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("New password and confirm password do not match");
        }

        // Get current user
        String username = getCurrentUsername(authHeader);
        StaffUser user = staffUserRepository.findByEmailOrPhone(username, username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect");
        }

        // Update password
        user.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        staffUserRepository.save(user);

        // Invalidate all tokens
        logout(authHeader);
    }

    private JwtResponse createJwtResponse(StaffUser user, Authentication authentication) {
        // Update last login
        staffUserRepository.updateLastLogin(user.getStaffId(), LocalDateTime.now());

        // Generate tokens with proper username and claims
        String username = user.getEmail() != null ? user.getEmail() : user.getPhone();
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", user.getRole().getCode());
        claims.put("userId", user.getStaffId());
        claims.put("fullName", user.getFullName());

        String accessToken = jwtUtils.generateToken(username, claims);
        String refreshToken = jwtUtils.generateRefreshToken(username);

        // Store refresh token in Redis
        tokenBlacklistService.storeRefreshToken(
                username,
                refreshToken,
                Duration.ofMillis(jwtUtils.getRefreshExpirationTime()));

        return buildJwtResponse(user, accessToken, refreshToken);
    }

    private JwtResponse buildJwtResponse(StaffUser user, String accessToken, String refreshToken) {
        return JwtResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtUtils.getExpirationTime() / 1000) // Convert to seconds
                .expiresAt(LocalDateTime.now().plusSeconds(jwtUtils.getExpirationTime() / 1000))
                .userInfo(JwtResponse.UserInfo.builder()
                        .id(user.getStaffId())
                        .fullName(user.getFullName())
                        .email(user.getEmail())
                        .phone(user.getPhone())
                        .role(user.getRole().getCode())
                        .isActive(user.getStatus() == StaffStatus.ACTIVE)
                        .build())
                .build();
    }

    private String getCurrentUsername(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            return jwtUtils.getUsernameFromToken(token);
        }
        throw new UnauthorizedException("Invalid authorization header");
    }

    @Override
    public JwtResponse authenticate(AuthRequest request) {
        return login(request);
    }

    @Override
    public void sendOtp(OtpRequest request) {
        requestOtp(request);
    }

    @Override
    public JwtResponse verifyOtp(OtpVerifyRequest request) {
        return verifyOtpAndLogin(request);
    }

    @Override
    public void changePassword(ChangePasswordRequest request) {
        throw new BadRequestException(
                "changePassword requires authorization header. Use changePassword(request, authHeader) instead.");
    }

    @Override
    public boolean validateToken(String token) {
        try {
            return jwtUtils.validateToken(token) && !tokenBlacklistService.isTokenBlacklisted(token);
        } catch (Exception e) {
            return false;
        }
    }
}
