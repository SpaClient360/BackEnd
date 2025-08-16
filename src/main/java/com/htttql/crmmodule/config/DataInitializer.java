package com.htttql.crmmodule.config;

import com.htttql.crmmodule.user.service.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Data initializer for default data setup
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleService roleService;

    @Override
    public void run(String... args) throws Exception {
        try {
            // Initialize default roles
            roleService.initializeDefaultRoles();
            log.info("Default roles initialized successfully");
        } catch (Exception e) {
            log.error("Error initializing default data: {}", e.getMessage());
        }
    }
}