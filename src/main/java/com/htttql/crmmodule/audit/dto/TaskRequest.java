package com.htttql.crmmodule.audit.dto;

import com.htttql.crmmodule.common.enums.TaskType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Task Request
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequest {

    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;

    @NotNull(message = "Assigned to is required")
    private Long assignedTo;

    @NotNull(message = "Task type is required")
    private TaskType type;

    @NotNull(message = "Priority is required")
    private Integer priority;

    @NotNull(message = "Due date is required")
    private LocalDateTime dueDate;
}
