package com.devops.app.integration;

import com.devops.app.dto.TaskRequest;
import com.devops.app.model.Task;
import com.devops.app.repository.AuditLogRepository;
import com.devops.app.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuditIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TaskRepository taskRepository;
    @Autowired AuditLogRepository auditLogRepository;

    @BeforeEach
    void clean() {
        auditLogRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    void createTaskProducesAuditEntry() throws Exception {
        TaskRequest req = new TaskRequest("Audit Task", "desc", Task.TaskStatus.TODO, Task.Priority.LOW);
        String body = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();

        Long taskId = objectMapper.readTree(body).get("id").asLong();

        // Audit is async — wait up to 3 seconds
        await().atMost(3, TimeUnit.SECONDS).untilAsserted(() -> {
            long count = auditLogRepository.findByEntityTypeAndEntityId("Task", taskId,
                org.springframework.data.domain.PageRequest.of(0, 10)).getTotalElements();
            assertThat(count).isGreaterThanOrEqualTo(1);
        });
    }
}
