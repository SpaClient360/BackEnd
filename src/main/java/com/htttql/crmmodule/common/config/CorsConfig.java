package com.htttql.crmmodule.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
public class CorsConfig {
        private static final List<String> ALLOWED_ORIGINS = List.of(
                        "http://localhost:3000",
                        "http://localhost:3001",
                        "http://localhost:4200",
                        "http://localhost:4201",
                        "http://localhost:5173",
                        "http://localhost:5174",
                        "http://localhost:8080",
                        "http://localhost:8081",
                        "http://127.0.0.1:3000",
                        "http://127.0.0.1:5173",
                        "http://127.0.0.1:5174");

        private static final List<String> ALLOWED_METHODS = Arrays.asList(
                        "GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD", "PATCH");

        private static final List<String> ALLOWED_HEADERS = List.of(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With",
                        "Accept",
                        "Origin",
                        "Access-Control-Request-Method",
                        "Access-Control-Request-Headers");

        private static final List<String> EXPOSED_HEADERS = List.of(
                        "Authorization",
                        "Content-Type",
                        "X-Requested-With",
                        "X-Total-Count");

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                configuration.setAllowedOrigins(ALLOWED_ORIGINS);

                configuration.setAllowedMethods(ALLOWED_METHODS);

                configuration.setAllowedHeaders(ALLOWED_HEADERS);

                configuration.setExposedHeaders(EXPOSED_HEADERS);

                configuration.setAllowCredentials(true);

                configuration.setMaxAge(3600L);

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);

                return source;
        }
}