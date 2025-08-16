package com.htttql.crmmodule.auth.service;

import com.htttql.crmmodule.auth.entity.RefreshToken;
import com.htttql.crmmodule.auth.repository.RefreshTokenRepository;
import com.htttql.crmmodule.auth.util.JwtUtil;
import com.htttql.crmmodule.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing refresh tokens
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtUtil jwtUtil;

    /**
     * Create refresh token for user
     * 
     * @param user the user to create refresh token for
     * @return created RefreshToken entity
     */
    @Transactional
    public RefreshToken createRefreshToken(User user) {
        // Revoke existing refresh tokens for this user to prevent token accumulation
        revokeAllUserTokens(user.getId());

        // Generate unique token using UUID + timestamp for uniqueness
        String tokenValue = UUID.randomUUID().toString() + "-" + System.currentTimeMillis();

        // Calculate expiration time
        LocalDateTime expiredAt = LocalDateTime.now()
                .plusSeconds(jwtUtil.getRefreshExpiration() / 1000);

        // Create and save refresh token
        RefreshToken refreshToken = new RefreshToken(tokenValue, user.getId(), expiredAt);
        RefreshToken savedToken = refreshTokenRepository.save(refreshToken);

        log.info("Refresh token created for user ID: {} with expiration: {}",
                user.getId(), expiredAt);

        return savedToken;
    }

    /**
     * Find valid refresh token by token string
     * 
     * @param token the refresh token string
     * @return Optional RefreshToken if valid, empty if invalid/expired
     */
    @Transactional(readOnly = true)
    public Optional<RefreshToken> findValidToken(String token) {
        return refreshTokenRepository.findValidToken(token, LocalDateTime.now());
    }

    /**
     * Verify and use refresh token
     * 
     * @param tokenValue the refresh token string
     * @return RefreshToken if valid and updated usage time
     * @throws RuntimeException if token is invalid or expired
     */
    @Transactional
    public RefreshToken verifyAndUseToken(String tokenValue) {
        RefreshToken refreshToken = findValidToken(tokenValue)
                .orElseThrow(() -> new RuntimeException("Refresh token không hợp lệ hoặc đã hết hạn"));

        // Mark token as used
        refreshToken.markAsUsed();
        RefreshToken updatedToken = refreshTokenRepository.save(refreshToken);

        log.info("Refresh token used for user ID: {}", refreshToken.getUserId());

        return updatedToken;
    }

    /**
     * Rotate refresh token - create new one and revoke old one
     * 
     * @param oldToken the old refresh token to replace
     * @param user     the user
     * @return new RefreshToken
     */
    @Transactional
    public RefreshToken rotateToken(RefreshToken oldToken, User user) {
        // Revoke the old token
        revokeToken(oldToken.getToken());

        // Create new refresh token
        RefreshToken newToken = createRefreshToken(user);

        log.info("Refresh token rotated for user ID: {}", user.getId());

        return newToken;
    }

    /**
     * Revoke specific refresh token
     * 
     * @param tokenValue the token to revoke
     */
    @Transactional
    public void revokeToken(String tokenValue) {
        refreshTokenRepository.revokeToken(tokenValue);
        log.info("Refresh token revoked: {}", maskToken(tokenValue));
    }

    /**
     * Revoke all refresh tokens for user
     * 
     * @param userId the user ID
     */
    @Transactional
    public void revokeAllUserTokens(Long userId) {
        refreshTokenRepository.revokeAllTokensByUserId(userId);
        log.info("All refresh tokens revoked for user ID: {}", userId);
    }

    /**
     * Delete refresh token
     * 
     * @param refreshToken the token to delete
     */
    @Transactional
    public void deleteToken(RefreshToken refreshToken) {
        refreshTokenRepository.delete(refreshToken);
        log.info("Refresh token deleted for user ID: {}", refreshToken.getUserId());
    }

    /**
     * Check if user has valid refresh tokens
     * 
     * @param userId the user ID
     * @return true if user has valid tokens
     */
    @Transactional(readOnly = true)
    public boolean hasValidTokens(Long userId) {
        long validTokenCount = refreshTokenRepository.countValidTokensByUserId(userId, LocalDateTime.now());
        return validTokenCount > 0;
    }

    /**
     * Clean up expired and revoked tokens (runs every hour)
     */
    @Scheduled(fixedRate = 3600000) // 1 hour
    @Transactional
    public void cleanupExpiredTokens() {
        try {
            refreshTokenRepository.deleteExpiredAndRevokedTokens(LocalDateTime.now());
            log.debug("Expired and revoked refresh tokens cleaned up");
        } catch (Exception e) {
            log.error("Failed to cleanup expired refresh tokens: {}", e.getMessage());
        }
    }

    /**
     * Get token statistics for monitoring
     * 
     * @return TokenStatistics object
     */
    @Transactional(readOnly = true)
    public TokenStatistics getTokenStatistics() {
        long totalTokens = refreshTokenRepository.count();
        return new TokenStatistics(totalTokens);
    }

    /**
     * Mask token for logging (security purpose)
     * 
     * @param token the token to mask
     * @return masked token
     */
    private String maskToken(String token) {
        if (token == null || token.length() < 8) {
            return "***";
        }
        return token.substring(0, 4) + "***" + token.substring(token.length() - 4);
    }

    /**
     * Token statistics inner class
     */
    public static class TokenStatistics {
        private final long totalTokens;

        public TokenStatistics(long totalTokens) {
            this.totalTokens = totalTokens;
        }

        public long getTotalTokens() {
            return totalTokens;
        }
    }
}