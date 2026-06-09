package com.devops.app.dto;

import com.devops.app.model.Task;
import jakarta.validation.constraints.*;

public record TaskRequest(
    @NotBlank(message = "Title is required")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    String title,

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    String description,

    Task.TaskStatus status,

    Task.Priority priority
) {
    public TaskRequest {
        if (status == null) status = Task.TaskStatus.TODO;
        if (priority == null) priority = Task.Priority.MEDIUM;
    }
}
