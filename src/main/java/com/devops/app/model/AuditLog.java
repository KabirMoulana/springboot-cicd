package com.devops.app.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log", indexes = {
    @Index(name = "idx_audit_entity", columnList = "entity_type,entity_id"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(nullable = false, length = 20)
    private String action; // CREATE, UPDATE, DELETE

    @Column(columnDefinition = "TEXT")
    private String details;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    public AuditLog() {}

    public AuditLog(String entityType, Long entityId, String action, String details) {
        this.entityType = entityType;
        this.entityId = entityId;
        this.action = action;
        this.details = details;
    }

    public Long getId() { return id; }
    public String getEntityType() { return entityType; }
    public Long getEntityId() { return entityId; }
    public String getAction() { return action; }
    public String getDetails() { return details; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
