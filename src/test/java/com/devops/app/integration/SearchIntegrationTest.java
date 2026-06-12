package com.devops.app.integration;

import com.devops.app.dto.TaskRequest;
import com.devops.app.model.Task;
import com.devops.app.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.devops.app.config.TestAsyncConfig;
import org.junit.jupiter.api.BeforeEach;
import com.devops.app.config.TestAsyncConfig;
import org.junit.jupiter.api.DisplayName;
import com.devops.app.config.TestAsyncConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import org.hamcrest.Matchers;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(TestAsyncConfig.class)
@DisplayName("Task Search integration tests")
class SearchIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TaskRepository taskRepository;

    @BeforeEach
    void seed() throws Exception {
        taskRepository.deleteAll();
        createTask("Fix login bug", Task.TaskStatus.TODO, Task.Priority.CRITICAL);
        createTask("Add dark mode", Task.TaskStatus.TODO, Task.Priority.LOW);
        createTask("Write API docs", Task.TaskStatus.IN_PROGRESS, Task.Priority.MEDIUM);
        createTask("Fix performance issue", Task.TaskStatus.DONE, Task.Priority.HIGH);
    }

    @Test
    @DisplayName("search by title (partial, case-insensitive)")
    void searchByTitle() throws Exception {
        mockMvc.perform(get("/api/tasks?title=fix"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2))
            .andExpect(jsonPath("$.content[0].title").value(Matchers.containsString("Fix")));
    }

    @Test
    @DisplayName("search by status=TODO")
    void searchByStatus() throws Exception {
        mockMvc.perform(get("/api/tasks?status=TODO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("search by priority=CRITICAL")
    void searchByPriority() throws Exception {
        mockMvc.perform(get("/api/tasks?priority=CRITICAL"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("search by title + status combined")
    void searchCombined() throws Exception {
        mockMvc.perform(get("/api/tasks?title=fix&status=TODO"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    @DisplayName("pagination returns correct pages")
    void pagination() throws Exception {
        mockMvc.perform(get("/api/tasks?page=0&size=2&sortBy=priority&sortDir=asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content.length()").value(2))
            .andExpect(jsonPath("$.totalPages").value(2))
            .andExpect(jsonPath("$.last").value(false));
    }

    private void createTask(String title, Task.TaskStatus status, Task.Priority priority) throws Exception {
        TaskRequest req = new TaskRequest(title, null, status, priority);
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated());
    }
}
