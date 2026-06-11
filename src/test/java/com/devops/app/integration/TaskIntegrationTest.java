package com.devops.app.integration;

import com.devops.app.dto.TaskRequest;
import com.devops.app.model.Task;
import com.devops.app.repository.AuditLogRepository;
import com.devops.app.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.devops.app.config.TestAsyncConfig;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestAsyncConfig.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DisplayName("Task API integration tests")
class TaskIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TaskRepository taskRepository;
    @Autowired AuditLogRepository auditLogRepository;

    @BeforeEach
    void cleanDb() {
        auditLogRepository.deleteAll();
        taskRepository.deleteAll();
    }

    @Test
    @Order(1)
    @DisplayName("full CRUD lifecycle")
    void fullCrudLifecycle() throws Exception {
        // CREATE
        TaskRequest createReq = new TaskRequest("Integration Task", "IT desc",
            Task.TaskStatus.TODO, Task.Priority.HIGH);
        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createReq)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Integration Task"))
            .andReturn();

        String body = createResult.getResponse().getContentAsString();
        Long id = objectMapper.readTree(body).get("id").asLong();
        assertThat(id).isNotNull();

        // READ
        mockMvc.perform(get("/api/tasks/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(id));

        // UPDATE
        TaskRequest updateReq = new TaskRequest("Updated Task", "Updated desc",
            Task.TaskStatus.DONE, Task.Priority.LOW);
        mockMvc.perform(put("/api/tasks/" + id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateReq)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DONE"));

        // DELETE
        mockMvc.perform(delete("/api/tasks/" + id))
            .andExpect(status().isNoContent());

        // VERIFY DELETED
        mockMvc.perform(get("/api/tasks/" + id))
            .andExpect(status().isNotFound());
    }

    @Test
    @Order(2)
    @DisplayName("GET /api/tasks returns paginated list")
    void paginatedList() throws Exception {
        // seed 3 tasks
        for (int i = 1; i <= 3; i++) {
            TaskRequest req = new TaskRequest("Task " + i, null,
                Task.TaskStatus.TODO, Task.Priority.MEDIUM);
            mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated());
        }

        mockMvc.perform(get("/api/tasks?page=0&size=2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.totalElements").value(3))
            .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    @Order(3)
    @DisplayName("POST /api/tasks returns 400 on blank title")
    void validationRejects() throws Exception {
        TaskRequest bad = new TaskRequest("", null, null, null);
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(bad)))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    @Order(4)
    @DisplayName("GET /api/tasks/stats returns status summary")
    void statsEndpoint() throws Exception {
        TaskRequest req = new TaskRequest("Stats Task", null,
            Task.TaskStatus.TODO, Task.Priority.LOW);
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());

        mockMvc.perform(get("/api/tasks/stats"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.TODO").value(1));
    }
}
