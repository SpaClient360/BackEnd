package com.htttql.crmmodule.auth.service;

import com.htttql.crmmodule.auth.dto.AuthResponse;
import com.htttql.crmmodule.auth.dto.LoginRequest;
import com.htttql.crmmodule.auth.dto.LoginOTPResponse;
import com.htttql.crmmodule.auth.dto.RefreshTokenRequest;
import com.htttql.crmmodule.auth.dto.RefreshTokenResponse;
import com.htttql.crmmodule.auth.dto.RegisterRequest;
import com.htttql.crmmodule.auth.dto.VerifyOTPRequest;
import com.htttql.crmmodule.auth.entity.RefreshToken;
import com.htttql.crmmodule.auth.service.UserDetailsServiceImpl.UserPrincipal;
import com.htttql.crmmodule.auth.util.JwtUtil;
import com.htttql.crmmodule.user.dto.UserDTO;
import com.htttql.crmmodule.user.entity.Role;
import com.htttql.crmmodule.user.entity.User;
import com.htttql.crmmodule.user.enums.UserStatus;
import com.htttql.crmmodule.user.repository.RoleRepository;
import com.htttql.crmmodule.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * Authentication service
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final OTPService otpService;
    private final RefreshTokenService refreshTokenService;

    /**
     * Authenticate user and return JWT token
     */
    @Transactional
    public AuthResponse login(LoginRequest request) {
        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmailOrPhone(), request.getPassword()));

            UserPrincipal userPrincipal = (UserPrincipal) authentication.getPrincipal();
            User user = userPrincipal.getUser();

            // Generate JWT token
            Map<String, Object> extraClaims = new HashMap<>();
            if (user.getRole() != null) {
                extraClaims.put("role", user.getRole().getRoleName());
            }
            extraClaims.put("userId", user.getId());

            String accessToken = jwtUtil.generateToken(extraClaims, userPrincipal);

            // Convert user to DTO
            UserDTO userDTO = convertToUserDTO(user);

            // Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return AuthResponse.of(accessToken, refreshToken.getToken(), jwtUtil.getJwtExpiration(), userDTO);

        } catch (Exception e) {
            throw new BadCredentialsException("Invalid email/phone or password");
        }
    }

    /**
     * Register new user
     */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // Check if email already exists
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already exists: " + request.getEmail());
        }

        // Check if phone already exists
        if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
            throw new RuntimeException("Phone already exists: " + request.getPhone());
        }

        // Get or create default role
        Role userRole = roleRepository.findByRoleName(request.getRoleName() != null ? request.getRoleName() : "USER")
                .orElseGet(() -> {
                    Role defaultRole = new Role();
                    defaultRole.setRoleName("USER");
                    defaultRole.setDescription("Regular user with minimal access");
                    return roleRepository.save(defaultRole);
                });

        // Create user with all information
        User user = new User();
        // Login information
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRoleId(userRole.getId());
        user.setStatus(UserStatus.ACTIVE);

        // Staff profile information
        user.setFullName(request.getFullName());
        user.setGender(request.getGender());
        user.setDateOfBirth(request.getDateOfBirth());
        user.setPosition(request.getPosition());
        user.setAddress(request.getAddress());
        user.setNote(request.getNote());

        User savedUser = userRepository.save(user);

        // Generate JWT token
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", userRole.getRoleName());
        extraClaims.put("userId", savedUser.getId());

        UserPrincipal userPrincipal = new UserPrincipal(savedUser);
        String accessToken = jwtUtil.generateToken(extraClaims, userPrincipal);

        // Convert user to DTO
        UserDTO userDTO = convertToUserDTO(savedUser);

        return AuthResponse.of(accessToken, jwtUtil.getJwtExpiration(), userDTO);
    }

    /**
     * Refresh JWT token
     */
    public AuthResponse refreshToken(String token) {
        if (jwtUtil.validateToken(token)) {
            String username = jwtUtil.extractUsername(token);
            User user = userRepository.findByEmailOrPhone(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));

            UserPrincipal userPrincipal = new UserPrincipal(user);

            Map<String, Object> extraClaims = new HashMap<>();
            if (user.getRole() != null) {
                extraClaims.put("role", user.getRole().getRoleName());
            }
            extraClaims.put("userId", user.getId());

            String newToken = jwtUtil.generateToken(extraClaims, userPrincipal);
            UserDTO userDTO = convertToUserDTO(user);

            return AuthResponse.of(newToken, jwtUtil.getJwtExpiration(), userDTO);
        } else {
            throw new RuntimeException("Invalid or expired token");
        }
    }

    /**
     * Get current user info
     */
    @Transactional(readOnly = true)
    public UserDTO getCurrentUser(String emailOrPhone) {
        User user = userRepository.findByEmailOrPhone(emailOrPhone)
                .orElseThrow(() -> new RuntimeException("User not found: " + emailOrPhone));

        return convertToUserDTO(user);
    }

    /**
     * Login with OTP - Step 1: Verify credentials and send OTP
     * 
     * @param request login request with email/phone and password
     * @return LoginOTPResponse with success/error message
     */
    @Transactional
    public LoginOTPResponse loginWithOTP(LoginRequest request) {
        try {
            // Find user by email or phone
            User user = userRepository.findByEmailOrPhone(request.getEmailOrPhone())
                    .orElse(null);

            if (user == null) {
                log.warn("Login attempt with non-existent email/phone: {}",
                        maskSensitiveInfo(request.getEmailOrPhone()));
                return LoginOTPResponse.error("Sai tài khoản hoặc mật khẩu");
            }

            // Check if user is active
            if (user.getStatus() != UserStatus.ACTIVE) {
                log.warn("Login attempt with inactive user: {}", user.getId());
                return LoginOTPResponse.error("Tài khoản đã bị khóa hoặc không hoạt động");
            }

            // Verify password
            if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
                log.warn("Invalid password attempt for user ID: {}", user.getId());
                return LoginOTPResponse.error("Sai tài khoản hoặc mật khẩu");
            }

            // Check if user has email for OTP
            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                log.warn("User ID {} does not have email for OTP", user.getId());
                return LoginOTPResponse.error("Tài khoản chưa có email để nhận OTP");
            }

            // Generate and send OTP
            otpService.generateAndSendOTP(user.getId(), user.getEmail());

            log.info("OTP login initiated for user ID: {}", user.getId());

            // Get remaining time for OTP
            long remainingMinutes = otpService.getRemainingMinutes(user.getId());

            return LoginOTPResponse.success("OTP đã được gửi đến email", remainingMinutes, user.getEmail());

        } catch (Exception e) {
            log.error("Error during OTP login: {}", e.getMessage());
            return LoginOTPResponse.error("Có lỗi xảy ra. Vui lòng thử lại sau.");
        }
    }

    /**
     * Verify OTP and generate JWT token - Step 2: Complete login process
     * 
     * @param request OTP verification request
     * @return AuthResponse with JWT token if OTP is valid
     */
    @Transactional
    public AuthResponse verifyOTPAndLogin(VerifyOTPRequest request) {
        try {
            // Find user by email or phone
            User user = userRepository.findByEmailOrPhone(request.getEmailOrPhone())
                    .orElseThrow(() -> new BadCredentialsException("User not found"));

            // Verify OTP
            boolean isValidOTP = otpService.verifyOTP(user.getId(), request.getOtpCode());

            if (!isValidOTP) {
                log.warn("Invalid OTP verification attempt for user ID: {}", user.getId());
                throw new BadCredentialsException("OTP không hợp lệ hoặc đã hết hạn");
            }

            // Generate JWT token
            Map<String, Object> extraClaims = new HashMap<>();
            if (user.getRole() != null) {
                extraClaims.put("role", user.getRole().getRoleName());
            }
            extraClaims.put("userId", user.getId());

            UserPrincipal userPrincipal = new UserPrincipal(user);
            String accessToken = jwtUtil.generateToken(extraClaims, userPrincipal);

            // Convert user to DTO
            UserDTO userDTO = convertToUserDTO(user);

            log.info("OTP login completed successfully for user ID: {}", user.getId());

            // Create refresh token
            RefreshToken refreshToken = refreshTokenService.createRefreshToken(user);

            return AuthResponse.of(accessToken, refreshToken.getToken(), jwtUtil.getJwtExpiration(), userDTO);

        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during OTP verification: {}", e.getMessage());
            throw new RuntimeException("Có lỗi xảy ra. Vui lòng thử lại sau.");
        }
    }

    /**
     * Mask sensitive information for logging
     * 
     * @param info sensitive information to mask
     * @return masked information
     */
    private String maskSensitiveInfo(String info) {
        if (info == null || info.length() < 3) {
            return "***";
        }

        if (info.contains("@")) {
            // Email masking
            int atIndex = info.indexOf('@');
            String localPart = info.substring(0, atIndex);
            String domain = info.substring(atIndex);

            if (localPart.length() <= 2) {
                return localPart.charAt(0) + "***" + domain;
            }
            return localPart.substring(0, 1) + "***" + domain;
        } else {
            // Phone masking
            if (info.length() <= 4) {
                return info.charAt(0) + "***";
            }
            return info.substring(0, 2) + "***" + info.substring(info.length() - 2);
        }
    }

    /**
     * Refresh access token using refresh token
     * 
     * @param request refresh token request
     * @return RefreshTokenResponse with new tokens
     */
    @Transactional
    public RefreshTokenResponse refreshAccessToken(RefreshTokenRequest request) {
        try {
            // Verify and use refresh token
            RefreshToken refreshToken = refreshTokenService.verifyAndUseToken(request.getRefreshToken());

            // Get user by ID
            User user = userRepository.findById(refreshToken.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Check if user is still active
            if (user.getStatus() != UserStatus.ACTIVE) {
                refreshTokenService.revokeToken(request.getRefreshToken());
                throw new BadCredentialsException("Tài khoản đã bị khóa hoặc không hoạt động");
            }

            // Generate new access token
            Map<String, Object> extraClaims = new HashMap<>();
            if (user.getRole() != null) {
                extraClaims.put("role", user.getRole().getRoleName());
            }
            extraClaims.put("userId", user.getId());

            UserPrincipal userPrincipal = new UserPrincipal(user);
            String newAccessToken = jwtUtil.generateToken(extraClaims, userPrincipal);

            // Rotate refresh token (create new refresh token and revoke old one)
            RefreshToken newRefreshToken = refreshTokenService.rotateToken(refreshToken, user);

            log.info("Access token refreshed for user ID: {}", user.getId());

            return RefreshTokenResponse.of(newAccessToken, newRefreshToken.getToken(),
                    (long) jwtUtil.getJwtExpiration());

        } catch (BadCredentialsException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error during token refresh: {}", e.getMessage());
            throw new RuntimeException("Không thể làm mới token. Vui lòng đăng nhập lại.");
        }
    }

    /**
     * Logout user by revoking all refresh tokens
     * 
     * @param emailOrPhone user identifier
     */
    @Transactional
    public void logout(String emailOrPhone) {
        try {
            User user = userRepository.findByEmailOrPhone(emailOrPhone)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Revoke all refresh tokens for this user
            refreshTokenService.revokeAllUserTokens(user.getId());

            log.info("User logged out successfully, user ID: {}", user.getId());

        } catch (Exception e) {
            log.error("Error during logout: {}", e.getMessage());
            // Don't throw exception for logout, just log the error
        }
    }

    /**
     * Logout user using refresh token
     * 
     * @param refreshToken the refresh token to revoke
     */
    @Transactional
    public void logoutWithRefreshToken(String refreshToken) {
        try {
            // Find and verify refresh token
            RefreshToken token = refreshTokenService.findValidToken(refreshToken)
                    .orElse(null);

            if (token != null) {
                // Revoke all refresh tokens for this user
                refreshTokenService.revokeAllUserTokens(token.getUserId());
                log.info("User logged out successfully using refresh token, user ID: {}", token.getUserId());
            } else {
                // Even if token is invalid, don't throw exception for security reasons
                log.warn("Logout attempted with invalid refresh token");
            }

        } catch (Exception e) {
            log.error("Error during logout with refresh token: {}", e.getMessage());
            // Don't throw exception for logout, just log the error
        }
    }

    /**
     * Convert User entity to UserDTO
     */
    private UserDTO convertToUserDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .phone(user.getPhone())
                .roleId(user.getRoleId())
                .status(user.getStatus())
                .fullName(user.getFullName())
                .gender(user.getGender())
                .dateOfBirth(user.getDateOfBirth())
                .position(user.getPosition())
                .address(user.getAddress())
                .note(user.getNote())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}