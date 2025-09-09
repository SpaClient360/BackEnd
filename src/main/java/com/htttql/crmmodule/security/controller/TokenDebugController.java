package com.htttql.crmmodule.security.controller;

import com.htttql.crmmodule.common.dto.ApiResponse;
import com.htttql.crmmodule.security.jwt.JwtUtils;
import com.htttql.crmmodule.security.service.TokenBlacklistService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Debug controller for JWT token issues
 * Remove this in production!
 */
@Tag(name = "Token Debug", description = "Debug JWT token issues")
@RestController
@RequestMapping("/api/debug/token")
@RequiredArgsConstructor
@Slf4j
public class TokenDebugController {

    private final JwtUtils jwtUtils;
    private final TokenBlacklistService tokenBlacklistService;

    @Operation(summary = "Debug token information")
    @PostMapping("/info")
    public ResponseEntity<ApiResponse<Map<String, Object>>> debugToken(@RequestBody Map<String, String> request) {
        String token = request.get("token");
        Map<String, Object> debugInfo = new HashMap<>();

        try {
            // Current time info
            Date currentTime = new Date();
            debugInfo.put("currentServerTime", currentTime);
            debugInfo.put("currentServerTimeString", currentTime.toString());
            debugInfo.put("currentLocalDateTime", LocalDateTime.now());
            debugInfo.put("serverTimeZone", ZoneId.systemDefault().toString());

            // JWT Config
            debugInfo.put("jwtExpirationConfig", jwtUtils.getExpirationTime());
            debugInfo.put("jwtExpirationHours", jwtUtils.getExpirationTime() / 1000 / 60 / 60);

            if (token != null && !token.isEmpty()) {
                // Token validation
                boolean isValid = jwtUtils.validateToken(token);
                debugInfo.put("isTokenStructurallyValid", isValid);

                if (isValid) {
                    // Token details
                    String username = jwtUtils.getUsernameFromToken(token);
                    Date expiration = jwtUtils.getExpirationDateFromToken(token);

                    debugInfo.put("username", username);
                    debugInfo.put("tokenExpiration", expiration);
                    debugInfo.put("tokenExpirationString", expiration.toString());
                    debugInfo.put("isTokenExpired", expiration.before(currentTime));
                    debugInfo.put("timeUntilExpiration", expiration.getTime() - currentTime.getTime());
                    debugInfo.put("minutesUntilExpiration", (expiration.getTime() - currentTime.getTime()) / 1000 / 60);

                    // Blacklist check
                    boolean isBlacklisted = tokenBlacklistService.isTokenBlacklisted(token);
                    debugInfo.put("isBlacklisted", isBlacklisted);

                    // Overall validity
                    debugInfo.put("isOverallValid", isValid && !isBlacklisted && !expiration.before(currentTime));
                }
            }

        } catch (Exception e) {
            debugInfo.put("error", e.getMessage());
            debugInfo.put("errorClass", e.getClass().getSimpleName());
            log.error("Debug token error: ", e);
        }

        return ResponseEntity.ok(ApiResponse.success(debugInfo, "Token debug info"));
    }

    @Operation(summary = "Check Redis connection")
    @GetMapping("/redis-check")
    public ResponseEntity<ApiResponse<Map<String, Object>>> checkRedis() {
        Map<String, Object> redisInfo = new HashMap<>();

        try {
            // Test Redis connection using the service
            boolean connectionWorking = tokenBlacklistService.testConnection();
            redisInfo.put("redisConnectionWorking", connectionWorking);

            if (connectionWorking) {
                redisInfo.put("message", "Redis connection is working properly");
            } else {
                redisInfo.put("message", "Redis connection test failed");
            }

        } catch (Exception e) {
            redisInfo.put("redisConnectionWorking", false);
            redisInfo.put("redisError", e.getMessage());
            log.error("Redis connection error: ", e);
        }

        return ResponseEntity.ok(ApiResponse.success(redisInfo, "Redis check completed"));
    }
}
