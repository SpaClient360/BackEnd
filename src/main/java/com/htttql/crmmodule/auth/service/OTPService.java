package com.htttql.crmmodule.auth.service;

import com.htttql.crmmodule.auth.entity.OTP;
import com.htttql.crmmodule.auth.repository.OTPRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Service for managing OTP operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OTPService {

    private final OTPRepository otpRepository;
    private final EmailService emailService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${otp.expiration.minutes:5}")
    private int otpExpirationMinutes;

    /**
     * Generate and send OTP to user email
     * 
     * @param userId the user ID
     * @param email  the user email
     * @return the generated OTP code (for testing purposes only)
     */
    @Transactional
    public String generateAndSendOTP(Long userId, String email) {
        // Delete existing OTPs for this user to prevent spam
        otpRepository.deleteByUserId(userId);

        // Generate 6-digit OTP
        String otpCode = generateOTPCode();

        // Calculate expiration time
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(otpExpirationMinutes);

        // Save OTP to database
        OTP otp = new OTP(userId, otpCode, expiredAt);
        otpRepository.save(otp);

        // Send OTP via email asynchronously
        sendOTPAsync(email, otpCode);

        log.info("OTP generated for user ID: {} with expiration: {}", userId, expiredAt);

        return otpCode; // Return for testing purposes only
    }

    /**
     * Verify OTP code for user
     * 
     * @param userId  the user ID
     * @param otpCode the OTP code to verify
     * @return true if OTP is valid, false otherwise
     */
    @Transactional
    public boolean verifyOTP(Long userId, String otpCode) {
        Optional<OTP> otpOpt = otpRepository.findValidOTP(userId, otpCode, LocalDateTime.now());

        if (otpOpt.isPresent()) {
            // OTP is valid, delete it to prevent reuse
            otpRepository.delete(otpOpt.get());
            log.info("OTP verified and consumed for user ID: {}", userId);
            return true;
        }

        log.warn("Invalid or expired OTP attempt for user ID: {}", userId);
        return false;
    }

    /**
     * Generate 6-digit OTP code
     * 
     * @return 6-digit OTP string
     */
    private String generateOTPCode() {
        int otp = secureRandom.nextInt(900000) + 100000; // Generate 6-digit number (100000-999999)
        return String.valueOf(otp);
    }

    /**
     * Send OTP email asynchronously to improve response time
     * 
     * @param email   recipient email
     * @param otpCode OTP code to send
     */
    @Async
    private void sendOTPAsync(String email, String otpCode) {
        try {
            emailService.sendOTPEmail(email, otpCode);
        } catch (Exception e) {
            log.error("Failed to send OTP email asynchronously: {}", e.getMessage());
            // Don't throw exception in async method to avoid affecting main flow
        }
    }

    /**
     * Check if user has valid OTP
     * 
     * @param userId the user ID
     * @return true if user has valid OTP, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean hasValidOTP(Long userId) {
        long validOTPCount = otpRepository.countValidOTPsByUserId(userId, LocalDateTime.now());
        return validOTPCount > 0;
    }

    /**
     * Get remaining minutes for OTP expiration
     * 
     * @param userId the user ID
     * @return remaining minutes, or 0 if no valid OTP
     */
    @Transactional(readOnly = true)
    public long getRemainingMinutes(Long userId) {
        Optional<OTP> latestOTP = otpRepository.findFirstByUserIdOrderByCreatedAtDesc(userId);

        if (latestOTP.isPresent() && !latestOTP.get().isExpired()) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expiredAt = latestOTP.get().getExpiredAt();

            if (expiredAt.isAfter(now)) {
                return java.time.Duration.between(now, expiredAt).toMinutes();
            }
        }

        return 0;
    }

    /**
     * Clean up expired OTPs automatically (runs every hour)
     */
    @Scheduled(fixedRate = 3600000) // 1 hour = 3600000 ms
    @Transactional
    public void cleanupExpiredOTPs() {
        try {
            otpRepository.deleteExpiredOTPs(LocalDateTime.now());
            log.debug("Expired OTPs cleaned up successfully");
        } catch (Exception e) {
            log.error("Failed to cleanup expired OTPs: {}", e.getMessage());
        }
    }
}