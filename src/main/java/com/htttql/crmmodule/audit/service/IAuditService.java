package com.htttql.crmmodule.audit.service;

import com.htttql.crmmodule.audit.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Audit operations
 */
public interface IAuditService {

    /**
     * Get all audit logs with pagination
     */
    Page<AuditLogResponse> getAllAuditLogs(Pageable pageable);

    /**
     * Get audit log by ID
     */
    AuditLogResponse getAuditLogById(Long id);
}
