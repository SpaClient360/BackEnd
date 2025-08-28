package com.htttql.crmmodule.audit.dto;

import com.htttql.crmmodule.common.enums.AuditAction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Audit Log Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long logId;
    private Long userId;
    private AuditAction action;
    private String entityType;
    private Long entityId;
    private String oldValues;
    private String newValues;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
