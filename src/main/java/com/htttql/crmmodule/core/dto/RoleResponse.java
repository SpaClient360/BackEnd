package com.htttql.crmmodule.core.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Role Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleResponse {
    private Long roleId;
    private String name;
    private String description;
    private String permissions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
