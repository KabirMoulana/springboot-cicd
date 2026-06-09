package com.devops.app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks", indexes = {
    @Index(name = "idx_task_status", columnList = "status"),
    @Index(name = "idx_task_created", columnList = "created_at")
})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Title must not be blank")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters")
    @Column(nullable = false)
    private String title;

    @Size(max = 2000, message = "Description must be at most 2000 characters")
    @Column(length = 2000)
    private String description;

    @NotNull(message = "Status must not be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.TODO;

    @NotNull(message = "Priority must not be null")
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private Priority priority = Priority.MEDIUM;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    public enum TaskStatus {
        TODO, IN_PROGRESS, DONE, CANCELLED
    }

    public enum Priority {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    // Constructors
    public Task() {}

    public Task(String title, String description, TaskStatus status, Priority priority) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.priority = priority;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }
    public Priority getPriority() { return priority; }
    public void setPriority(Priority priority) { this.priority = priority; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    @Override
    public String toString() {
        return "Task{id=" + id + ", title='" + title + "', status=" + status + ", priority=" + priority + "}";
    }
}
