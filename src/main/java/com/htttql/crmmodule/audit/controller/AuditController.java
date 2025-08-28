package com.htttql.crmmodule.audit.controller;

import com.htttql.crmmodule.audit.dto.AuditLogResponse;
import com.htttql.crmmodule.audit.dto.TaskRequest;
import com.htttql.crmmodule.audit.dto.TaskResponse;
import com.htttql.crmmodule.audit.dto.RetouchScheduleRequest;
import com.htttql.crmmodule.audit.dto.RetouchScheduleResponse;
import com.htttql.crmmodule.audit.service.IAuditService;
import com.htttql.crmmodule.audit.service.ITaskService;
import com.htttql.crmmodule.audit.service.IRetouchScheduleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * Audit Management Controller
 * Manages audit logs, tasks, and retouch schedules
 */
@Tag(name = "Audit Management", description = "Audit logs, tasks, and retouch schedules management")
@RestController
@RequestMapping("/api/audit")
@RequiredArgsConstructor
public class AuditController {

    private final IAuditService auditService;
    private final ITaskService taskService;
    private final IRetouchScheduleService retouchScheduleService;

    // ==================== AUDIT LOGS ====================
    
    @Operation(summary = "Get all audit logs with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/logs")
    public ResponseEntity<Page<AuditLogResponse>> getAllAuditLogs(Pageable pageable) {
        Page<AuditLogResponse> logs = auditService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(logs);
    }

    @Operation(summary = "Get audit log by ID")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @GetMapping("/logs/{id}")
    public ResponseEntity<AuditLogResponse> getAuditLogById(@PathVariable Long id) {
        AuditLogResponse log = auditService.getAuditLogById(id);
        return ResponseEntity.ok(log);
    }

    // ==================== TASKS ====================
    
    @Operation(summary = "Get all tasks with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN')")
    @GetMapping("/tasks")
    public ResponseEntity<Page<TaskResponse>> getAllTasks(Pageable pageable) {
        Page<TaskResponse> tasks = taskService.getAllTasks(pageable);
        return ResponseEntity.ok(tasks);
    }

    @Operation(summary = "Create new task")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN')")
    @PostMapping("/tasks")
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request) {
        TaskResponse task = taskService.createTask(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(task);
    }

    @Operation(summary = "Update task")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN')")
    @PutMapping("/tasks/{id}")
    public ResponseEntity<TaskResponse> updateTask(
            @PathVariable Long id,
            @Valid @RequestBody TaskRequest request) {
        TaskResponse task = taskService.updateTask(id, request);
        return ResponseEntity.ok(task);
    }

    @Operation(summary = "Delete task")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/tasks/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update task status")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN')")
    @PutMapping("/tasks/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        TaskResponse task = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(task);
    }

    // ==================== RETOUCH SCHEDULES ====================
    
    @Operation(summary = "Get all retouch schedules with pagination")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN')")
    @GetMapping("/retouch-schedules")
    public ResponseEntity<Page<RetouchScheduleResponse>> getAllRetouchSchedules(Pageable pageable) {
        Page<RetouchScheduleResponse> schedules = retouchScheduleService.getAllRetouchSchedules(pageable);
        return ResponseEntity.ok(schedules);
    }

    @Operation(summary = "Create new retouch schedule")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN')")
    @PostMapping("/retouch-schedules")
    public ResponseEntity<RetouchScheduleResponse> createRetouchSchedule(
            @Valid @RequestBody RetouchScheduleRequest request) {
        RetouchScheduleResponse schedule = retouchScheduleService.createRetouchSchedule(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(schedule);
    }

    @Operation(summary = "Update retouch schedule")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN')")
    @PutMapping("/retouch-schedules/{id}")
    public ResponseEntity<RetouchScheduleResponse> updateRetouchSchedule(
            @PathVariable Long id,
            @Valid @RequestBody RetouchScheduleRequest request) {
        RetouchScheduleResponse schedule = retouchScheduleService.updateRetouchSchedule(id, request);
        return ResponseEntity.ok(schedule);
    }

    @Operation(summary = "Delete retouch schedule")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasRole('MANAGER')")
    @DeleteMapping("/retouch-schedules/{id}")
    public ResponseEntity<Void> deleteRetouchSchedule(@PathVariable Long id) {
        retouchScheduleService.deleteRetouchSchedule(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Update retouch schedule status")
    @SecurityRequirement(name = "Bearer Authentication")
    @PreAuthorize("hasAnyRole('MANAGER', 'TECHNICIAN')")
    @PutMapping("/retouch-schedules/{id}/status")
    public ResponseEntity<RetouchScheduleResponse> updateRetouchScheduleStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        RetouchScheduleResponse schedule = retouchScheduleService.updateRetouchScheduleStatus(id, status);
        return ResponseEntity.ok(schedule);
    }
}
