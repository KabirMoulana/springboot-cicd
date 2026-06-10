package com.devops.app.controller;

import com.devops.app.dto.PagedResponse;
import com.devops.app.model.AuditLog;
import com.devops.app.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/audit")
@Tag(name = "Audit", description = "Audit trail endpoints")
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    public AuditController(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @GetMapping
    @Operation(summary = "List all audit log entries")
    public ResponseEntity<PagedResponse<AuditLog>> listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, Math.min(size, 100), Sort.by("createdAt").descending());
        return ResponseEntity.ok(PagedResponse.from(auditLogRepository.findAll(pageable)));
    }

    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Get audit trail for a specific task")
    public ResponseEntity<PagedResponse<AuditLog>> getTaskAudit(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(
            PagedResponse.from(auditLogRepository.findByEntityTypeAndEntityId("Task", taskId, pageable)));
    }
}
