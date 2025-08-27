package com.htttql.crmmodule.security.service;

import com.htttql.crmmodule.core.entity.StaffUser;
import com.htttql.crmmodule.core.repository.StaffUserRepository;
import com.htttql.crmmodule.common.exception.BadRequestException;
import com.htttql.crmmodule.security.dto.OtpRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service for OTP generation and validation
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OtpService {

    private final StaffUserRepository staffUserRepository;

    private final RedisTemplate<String, String> redisTemplate;
    private final JavaMailSender mailSender;

    @Value("${otp.expiration.minutes:5}")
    private int otpExpirationMinutes;

    @Value("${spring.mail.username}")
    private String fromEmail;

    private static final String OTP_PREFIX = "OTP:";
    private static final String OTP_ATTEMPT_PREFIX = "OTP_ATTEMPT:";
    private static final int MAX_ATTEMPTS = 3;
    private static final SecureRandom random = new SecureRandom();

    public String generateAndSendOtp(String username, OtpRequest.OtpPurpose purpose) {
        String otpCode = generateOtp();
        String key = OTP_PREFIX + purpose + ":" + username;

        // Store OTP in Redis with expiration
        redisTemplate.opsForValue().set(key, otpCode, Duration.ofMinutes(otpExpirationMinutes));

        // Always send OTP to user's email (regardless of username type)
        // This ensures user receives OTP whether they use email or phone as username
        String userEmail = getUserEmailFromUsername(username);
        sendOtpByEmail(userEmail, otpCode, purpose);

        log.info("OTP sent to email: {} for user: {}", userEmail, username);
        return "OTP sent successfully to your email";
    }

    public boolean verifyOtp(String username, String otpCode, OtpRequest.OtpPurpose purpose) {
        log.info("Starting OTP verification for user: {}, purpose: {}", username, purpose);
        String key = OTP_PREFIX + purpose + ":" + username;
        String attemptKey = OTP_ATTEMPT_PREFIX + username;
        log.debug("Redis keys - OTP key: {}, Attempt key: {}", key, attemptKey);

        // Check attempts - safely parse with error handling
        String attempts = redisTemplate.opsForValue().get(attemptKey);
        int attemptCount = 0;
        try {
            if (attempts != null) {
                attemptCount = Integer.parseInt(attempts);
            }
        } catch (NumberFormatException e) {
            log.warn("Invalid attempt count format for user: {}, resetting to 0", username);
            // Reset invalid attempt count
            redisTemplate.delete(attemptKey);
            attemptCount = 0;
        }

        if (attemptCount >= MAX_ATTEMPTS) {
            log.warn("Max OTP attempts exceeded for user: {}", username);
            return false;
        }

        // Get stored OTP
        String storedOtp = redisTemplate.opsForValue().get(key);
        log.debug("Stored OTP from Redis: {} (null means expired/not found)", storedOtp);

        // Check if OTP exists (not expired)
        if (storedOtp == null) {
            log.warn("OTP not found or expired for user: {}", username);
            return false;
        }

        // Verify OTP code
        if (storedOtp.equals(otpCode)) {
            // OTP is valid - delete OTP and reset attempts
            redisTemplate.delete(key);
            redisTemplate.delete(attemptKey);
            log.info("OTP verified successfully for user: {}", username);
            return true;
        } else {
            // OTP is invalid - increment attempt count
            redisTemplate.opsForValue().set(attemptKey, String.valueOf(attemptCount + 1),
                    Duration.ofMinutes(otpExpirationMinutes));
            log.warn("Invalid OTP attempt {} for user: {}", attemptCount + 1, username);
            return false;
        }
    }

    private String generateOtp() {
        return String.format("%06d", random.nextInt(999999));
    }

    private boolean isEmail(String username) {
        return username.contains("@");
    }

    /**
     * Get user's email from username (email or phone)
     * 
     * @param username email or phone number
     * @return user's email address
     */
    private String getUserEmailFromUsername(String username) {
        try {
            StaffUser user = staffUserRepository.findByEmailOrPhone(username, username)
                    .orElseThrow(() -> new BadRequestException("User not found"));

            if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
                throw new BadRequestException("User does not have a valid email address");
            }

            return user.getEmail();
        } catch (Exception e) {
            log.error("Error getting user email for username: {}, error: {}", username, e.getMessage());
            throw new BadRequestException("Failed to get user information: " + e.getMessage());
        }
    }

    private void sendOtpByEmail(String email, String otpCode, OtpRequest.OtpPurpose purpose) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(email);
            message.setSubject("Your OTP Code - " + purpose.name());
            message.setText(buildOtpEmailContent(otpCode, purpose));

            mailSender.send(message);
            log.info("OTP email sent to: {}", email);
        } catch (Exception e) {
            log.error("Failed to send OTP email to {}: {}", email, e.getMessage());
            throw new RuntimeException("Failed to send OTP email");
        }
    }

    private String buildOtpEmailContent(String otpCode, OtpRequest.OtpPurpose purpose) {
        return String.format("""
                Hello,

                Your OTP code for %s is: %s

                This code will expire in %d minutes.

                If you did not request this code, please ignore this email.

                Best regards,
                CRM Spa Team
                """, purpose.name().toLowerCase().replace("_", " "), otpCode, otpExpirationMinutes);
    }
}
