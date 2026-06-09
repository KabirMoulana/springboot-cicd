package com.devops.app.exception;

public class TaskNotFoundException extends RuntimeException {
    private final Long taskId;

    public TaskNotFoundException(Long id) {
        super("Task not found with id: " + id);
        this.taskId = id;
    }

    public Long getTaskId() { return taskId; }
}
