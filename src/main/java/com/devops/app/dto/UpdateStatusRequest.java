package com.devops.app.dto;

import com.devops.app.model.Task;
import jakarta.validation.constraints.NotNull;

/**
 * Lightweight DTO for PATCH status-only updates.
 */
public record UpdateStatusRequest(
    @NotNull(message = "Status is required")
    Task.TaskStatus status
) {}
