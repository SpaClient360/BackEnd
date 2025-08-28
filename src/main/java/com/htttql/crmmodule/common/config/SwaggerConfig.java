package com.htttql.crmmodule.common.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI configuration for CRM Module
 * Access: http://localhost:8081/swagger-ui/index.html
 * 
 * Role-based Access Control:
 * - MANAGER: Full access to all endpoints
 * - TECHNICIAN: Access to technician-specific endpoints
 * - RECEPTIONIST: Access to receptionist-specific endpoints
 * - PUBLIC: No authentication required (auth, leads creation, account creation)
 */
@Configuration
@OpenAPIDefinition(info = @Info(title = "CRM Spa Management System API", description = "RESTful API for CRM system specialized in lip and eyebrow tattooing services. "
        +
        "This API supports role-based access control with the following roles: " +
        "MANAGER (full access), TECHNICIAN (service management), RECEPTIONIST (customer management), " +
        "and PUBLIC endpoints (authentication, lead creation).", version = "1.0.0", contact = @Contact(name = "CRM Development Team", email = "dev@crmspa.com", url = "https://github.com/your-repo/crm-module"), license = @License(name = "MIT License", url = "https://opensource.org/licenses/MIT")), servers = {
                @Server(description = "Local Development Server", url = "http://localhost:8081"),
                @Server(description = "Production Server", url = "https://api.crmspa.com")
        }, security = {
                @SecurityRequirement(name = "Bearer Authentication")
        })
@SecurityScheme(name = "Bearer Authentication", description = "JWT authentication with Bearer token. Format: Bearer {token}. "
        +
        "Required for most endpoints except public ones (auth, leads creation, account creation).", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT", in = SecuritySchemeIn.HEADER)
public class SwaggerConfig {

    // OpenAPI configuration is done through annotations above
    // Global security requirement is set to require authentication for all
    // endpoints
    // Individual endpoints can override this using @SecurityRequirement annotation
    // Public endpoints (auth, leads creation, account creation) don't need
    // @SecurityRequirement

}
