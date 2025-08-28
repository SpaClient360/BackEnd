package com.htttql.crmmodule.audit.repository;

import com.htttql.crmmodule.audit.entity.AuditLog;
import com.htttql.crmmodule.common.enums.AuditAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for AuditLog entity
 */
@Repository
public interface IAuditLogRepository extends JpaRepository<AuditLog, Long> {

    /**
     * Find audit logs by user ID
     */
    List<AuditLog> findByActor_StaffId(Long userId);

    /**
     * Find audit logs by entity name and entity ID
     */
    List<AuditLog> findByEntityNameAndEntityId(String entityName, Long entityId);

    /**
     * Find audit logs by action
     */
    List<AuditLog> findByAction(AuditAction action);
}
