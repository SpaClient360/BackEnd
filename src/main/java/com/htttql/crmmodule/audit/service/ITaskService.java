package com.htttql.crmmodule.audit.service;

import com.htttql.crmmodule.audit.dto.TaskRequest;
import com.htttql.crmmodule.audit.dto.TaskResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service interface for Task operations
 */
public interface ITaskService {

    /**
     * Get all tasks with pagination
     */
    Page<TaskResponse> getAllTasks(Pageable pageable);

    /**
     * Create new task
     */
    TaskResponse createTask(TaskRequest request);

    /**
     * Update task
     */
    TaskResponse updateTask(Long id, TaskRequest request);

    /**
     * Delete task
     */
    void deleteTask(Long id);

    /**
     * Update task status
     */
    TaskResponse updateTaskStatus(Long id, String status);
}
