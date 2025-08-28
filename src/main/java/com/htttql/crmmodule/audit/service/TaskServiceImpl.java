package com.htttql.crmmodule.audit.service;

import com.htttql.crmmodule.audit.dto.TaskRequest;
import com.htttql.crmmodule.audit.dto.TaskResponse;
import com.htttql.crmmodule.audit.entity.Task;
import com.htttql.crmmodule.common.enums.TaskStatus;
import com.htttql.crmmodule.audit.repository.ITaskRepository;
import com.htttql.crmmodule.common.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Implementation of Task Service
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements ITaskService {

    private final ITaskRepository taskRepository;

    @Override
    public Page<TaskResponse> getAllTasks(Pageable pageable) {
        Page<Task> tasks = taskRepository.findAll(pageable);
        return tasks.map(this::mapToResponse);
    }

    @Override
    public TaskResponse createTask(TaskRequest request) {
        Task task = Task.builder()
                .title(request.getTitle())
                .note(request.getDescription())
                .type(request.getType())
                .priority(request.getPriority())
                .dueAt(request.getDueDate())
                .status(TaskStatus.OPEN)
                .build();

        Task savedTask = taskRepository.save(task);
        return mapToResponse(savedTask);
    }

    @Override
    public TaskResponse updateTask(Long id, TaskRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setTitle(request.getTitle());
        task.setNote(request.getDescription());
        task.setType(request.getType());
        task.setPriority(request.getPriority());
        task.setDueAt(request.getDueDate());

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    @Override
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task not found with id: " + id);
        }
        taskRepository.deleteById(id);
    }

    @Override
    public TaskResponse updateTaskStatus(Long id, String status) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        TaskStatus taskStatus = TaskStatus.valueOf(status.toUpperCase());
        task.setStatus(taskStatus);

        if (taskStatus == TaskStatus.DONE) {
            task.setCompletedAt(LocalDateTime.now());
        }

        Task updatedTask = taskRepository.save(task);
        return mapToResponse(updatedTask);
    }

    private TaskResponse mapToResponse(Task task) {
        return TaskResponse.builder()
                .taskId(task.getTaskId())
                .title(task.getTitle())
                .description(task.getNote())
                .assignedTo(task.getAssignee() != null ? task.getAssignee().getStaffId() : null)
                .assignedBy(null) // Not available in entity
                .type(task.getType())
                .status(task.getStatus())
                .priority(task.getPriority())
                .dueDate(task.getDueAt())
                .completedAt(task.getCompletedAt())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
}
