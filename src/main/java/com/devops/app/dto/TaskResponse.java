package com.devops.app.dto;

import com.devops.app.model.Task;
import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    String title,
    String description,
    Task.TaskStatus status,
    Task.Priority priority,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
    public static TaskResponse from(Task task) {
        return new TaskResponse(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            task.getCreatedAt(),
            task.getUpdatedAt()
        );
    }
}
