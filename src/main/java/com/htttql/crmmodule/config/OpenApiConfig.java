package com.htttql.crmmodule.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(info = @Info(title = "CRM Module API", version = "v1", description = "API documentation for CRM Module"))
public class OpenApiConfig {
}
