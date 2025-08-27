package com.htttql.crmmodule.security.jwt;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * JWT Service for token management
 * This is a placeholder service that may be extended in the future
 */
@Slf4j
@Service
public class JwtService {

    // Placeholder service - currently using JwtUtils for actual JWT operations
    // This service can be extended for additional JWT-related functionality

    public void logTokenGeneration(String username) {
        log.info("JWT token generated for user: {}", username);
    }

    public void logTokenValidation(String username) {
        log.debug("JWT token validated for user: {}", username);
    }
}