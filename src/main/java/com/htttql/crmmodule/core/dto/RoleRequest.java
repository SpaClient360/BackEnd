package com.htttql.crmmodule.core.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Role Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 50, message = "Name must not exceed 50 characters")
    private String name;

    @Size(max = 200, message = "Description must not exceed 200 characters")
    private String description;

    @Size(max = 500, message = "Permissions must not exceed 500 characters")
    private String permissions;
}
