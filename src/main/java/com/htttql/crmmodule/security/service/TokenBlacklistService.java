package com.htttql.crmmodule.security.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * Service to handle JWT token blacklisting with Redis fallback
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String BLACKLIST_PREFIX = "BLACKLIST:";
    private static final String REFRESH_TOKEN_PREFIX = "REFRESH:";

    /**
     * Add token to blacklist
     */
    public void blacklistToken(String token, long expirationMs) {
        try {
            if (expirationMs > 0) {
                redisTemplate.opsForValue().set(
                        BLACKLIST_PREFIX + token,
                        "true",
                        expirationMs,
                        TimeUnit.MILLISECONDS);
                log.debug("Token blacklisted successfully");
            }
        } catch (Exception e) {
            log.error("Failed to blacklist token: {}", e.getMessage());
            // In production, you might want to store in database as fallback
        }
    }

    /**
     * Check if token is blacklisted
     */
    public boolean isTokenBlacklisted(String token) {
        try {
            Boolean result = redisTemplate.hasKey(BLACKLIST_PREFIX + token);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to check token blacklist: {}", e.getMessage());
            // If Redis fails, assume token is not blacklisted to avoid false positives
            return false;
        }
    }

    /**
     * Store refresh token
     */
    public void storeRefreshToken(String username, String refreshToken, Duration expiration) {
        try {
            redisTemplate.opsForValue().set(
                    REFRESH_TOKEN_PREFIX + username,
                    refreshToken,
                    expiration);
            log.debug("Refresh token stored for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to store refresh token for user {}: {}", username, e.getMessage());
        }
    }

    /**
     * Get refresh token
     */
    public String getRefreshToken(String username) {
        try {
            return redisTemplate.opsForValue().get(REFRESH_TOKEN_PREFIX + username);
        } catch (Exception e) {
            log.error("Failed to get refresh token for user {}: {}", username, e.getMessage());
            return null;
        }
    }

    /**
     * Remove refresh token
     */
    public void removeRefreshToken(String username) {
        try {
            redisTemplate.delete(REFRESH_TOKEN_PREFIX + username);
            log.debug("Refresh token removed for user: {}", username);
        } catch (Exception e) {
            log.error("Failed to remove refresh token for user {}: {}", username, e.getMessage());
        }
    }

    /**
     * Test Redis connection
     */
    public boolean testConnection() {
        try {
            redisTemplate.opsForValue().set("connection-test", "ok");
            String result = redisTemplate.opsForValue().get("connection-test");
            redisTemplate.delete("connection-test");
            return "ok".equals(result);
        } catch (Exception e) {
            log.error("Redis connection test failed: {}", e.getMessage());
            return false;
        }
    }
}
