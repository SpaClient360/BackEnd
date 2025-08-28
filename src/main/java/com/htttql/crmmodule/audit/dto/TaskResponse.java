package com.htttql.crmmodule.audit.dto;

import com.htttql.crmmodule.common.enums.TaskStatus;
import com.htttql.crmmodule.common.enums.TaskType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Task Response
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskResponse {
    private Long taskId;
    private String title;
    private String description;
    private Long assignedTo;
    private Long assignedBy;
    private TaskType type;
    private TaskStatus status;
    private Integer priority;
    private LocalDateTime dueDate;
    private LocalDateTime completedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
