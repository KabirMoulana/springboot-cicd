package com.devops.app.service;

import com.devops.app.model.AuditLog;
import com.devops.app.repository.AuditLogRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock AuditLogRepository auditLogRepository;
    @InjectMocks AuditService auditService;

    @Test
    void logSavesCorrectAuditEntry() {
        auditService.log("Task", 42L, "CREATE", "title=MyTask");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(auditLogRepository).save(captor.capture());

        AuditLog saved = captor.getValue();
        assertThat(saved.getEntityType()).isEqualTo("Task");
        assertThat(saved.getEntityId()).isEqualTo(42L);
        assertThat(saved.getAction()).isEqualTo("CREATE");
        assertThat(saved.getDetails()).isEqualTo("title=MyTask");
    }

    @Test
    void logWithNullDetailsSavesWithoutThrowing() {
        auditService.log("Task", 1L, "DELETE", null);
        verify(auditLogRepository, times(1)).save(any(AuditLog.class));
    }
}
