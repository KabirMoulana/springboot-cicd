package com.devops.app.integration;

import com.devops.app.dto.TaskRequest;
import com.devops.app.dto.UpdateStatusRequest;
import com.devops.app.model.Task;
import com.devops.app.repository.TaskRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("PATCH /api/tasks/{id}/status integration tests")
class TaskPatchIntegrationTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @Autowired TaskRepository taskRepository;

    @BeforeEach
    void clean() { taskRepository.deleteAll(); }

    private Long createTask(String title) throws Exception {
        TaskRequest req = new TaskRequest(title, null, Task.TaskStatus.TODO, Task.Priority.MEDIUM);
        String body = mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isCreated())
            .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("id").asLong();
    }

    @Test
    @DisplayName("PATCH status to DONE returns 200 with updated status")
    void patchStatusToDone() throws Exception {
        Long id = createTask("Patch Me");

        UpdateStatusRequest patch = new UpdateStatusRequest(Task.TaskStatus.DONE);
        mockMvc.perform(patch("/api/tasks/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DONE"))
            .andExpect(jsonPath("$.id").value(id));
    }

    @Test
    @DisplayName("PATCH status to non-existent task returns 404")
    void patchMissingTaskReturns404() throws Exception {
        UpdateStatusRequest patch = new UpdateStatusRequest(Task.TaskStatus.IN_PROGRESS);
        mockMvc.perform(patch("/api/tasks/99999/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH null status returns 400 validation error")
    void patchNullStatusReturns400() throws Exception {
        Long id = createTask("Valid Task");
        mockMvc.perform(patch("/api/tasks/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":null}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation Error"));
    }

    @Test
    @DisplayName("PATCH persists status change to database")
    void patchPersistsToDb() throws Exception {
        Long id = createTask("DB Persist Test");
        UpdateStatusRequest patch = new UpdateStatusRequest(Task.TaskStatus.CANCELLED);
        mockMvc.perform(patch("/api/tasks/" + id + "/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(patch)))
            .andExpect(status().isOk());

        // Verify directly via GET
        mockMvc.perform(get("/api/tasks/" + id))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
