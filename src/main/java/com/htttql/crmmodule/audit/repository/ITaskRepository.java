package com.htttql.crmmodule.audit.repository;

import com.htttql.crmmodule.audit.entity.Task;
import com.htttql.crmmodule.common.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository interface for Task entity
 */
@Repository
public interface ITaskRepository extends JpaRepository<Task, Long> {

    /**
     * Find tasks by assigned to user
     */
    List<Task> findByAssignee_StaffId(Long assignedTo);

    /**
     * Find tasks by status
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Find tasks by assigned to user and status
     */
    List<Task> findByAssignee_StaffIdAndStatus(Long assignedTo, TaskStatus status);
}
