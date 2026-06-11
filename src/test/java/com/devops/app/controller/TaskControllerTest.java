package com.devops.app.controller;


import com.devops.app.dto.PagedResponse;
import com.devops.app.dto.TaskRequest;
import com.devops.app.dto.TaskResponse;
import com.devops.app.config.TestSecurityConfig;
import com.devops.app.exception.GlobalExceptionHandler;
import com.devops.app.exception.TaskNotFoundException;
import com.devops.app.model.Task;
import com.devops.app.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("TaskController web layer tests")
class TaskControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean TaskService taskService;

    private TaskResponse sampleResponse() {
        return new TaskResponse(1L, "Test Task", "Description",
            Task.TaskStatus.TODO, Task.Priority.MEDIUM,
            LocalDateTime.now(), LocalDateTime.now());
    }

    @Nested
    @DisplayName("GET /api/tasks")
    class GetAll {
        @Test
        @DisplayName("returns 200 with paged task list")
        void returns200() throws Exception {
            PagedResponse<TaskResponse> paged = new PagedResponse<>(
                List.of(sampleResponse()), 0, 20, 1, 1, true);
            when(taskService.findAll(any())).thenReturn(paged);

            mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1));
        }
    }

    @Nested
    @DisplayName("GET /api/tasks/{id}")
    class GetById {
        @Test
        @DisplayName("returns 200 for existing task")
        void returns200() throws Exception {
            when(taskService.findById(1L)).thenReturn(sampleResponse());
            mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
        }

        @Test
        @DisplayName("returns 404 for missing task")
        void returns404() throws Exception {
            when(taskService.findById(99L)).thenThrow(new TaskNotFoundException(99L));
            mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Task Not Found"));
        }
    }

    @Nested
    @DisplayName("POST /api/tasks")
    class Create {
        @Test
        @DisplayName("returns 201 with Location header on valid request")
        void returns201() throws Exception {
            TaskRequest req = new TaskRequest("New Task", "Desc", Task.TaskStatus.TODO, Task.Priority.HIGH);
            when(taskService.create(any())).thenReturn(sampleResponse());

            mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.id").value(1));
        }

        @Test
        @DisplayName("returns 400 on blank title")
        void returns400OnBlankTitle() throws Exception {
            TaskRequest req = new TaskRequest("", null, null, null);
            mockMvc.perform(post("/api/tasks")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation Error"));
        }
    }

    @Nested
    @DisplayName("PUT /api/tasks/{id}")
    class Update {
        @Test
        @DisplayName("returns 200 on valid update")
        void returns200() throws Exception {
            TaskRequest req = new TaskRequest("Updated", "Desc", Task.TaskStatus.DONE, Task.Priority.LOW);
            when(taskService.update(eq(1L), any())).thenReturn(sampleResponse());

            mockMvc.perform(put("/api/tasks/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/tasks/{id}")
    class DeleteTask {
        @Test
        @DisplayName("returns 204 on successful delete")
        void returns204() throws Exception {
            doNothing().when(taskService).delete(1L);
            mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("returns 404 when deleting missing task")
        void returns404() throws Exception {
            doThrow(new TaskNotFoundException(99L)).when(taskService).delete(99L);
            mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound());
        }
    }
}
