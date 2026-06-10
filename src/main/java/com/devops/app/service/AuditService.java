package com.devops.app.service;

import com.devops.app.model.AuditLog;
import com.devops.app.repository.AuditLogRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuditService {

    private static final Logger log = LoggerFactory.getLogger(AuditService.class);
    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Async
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(String entityType, Long entityId, String action, String details) {
        try {
            AuditLog entry = new AuditLog(entityType, entityId, action, details);
            auditLogRepository.save(entry);
            log.debug("Audit: {} {} id={}", action, entityType, entityId);
        } catch (Exception e) {
            // Audit failures must never break the main transaction
            log.error("Failed to write audit log for {} {} id={}: {}", action, entityType, entityId, e.getMessage());
        }
    }
}
