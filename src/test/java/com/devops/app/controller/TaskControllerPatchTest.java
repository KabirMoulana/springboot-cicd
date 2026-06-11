package com.devops.app.controller;


import com.devops.app.dto.TaskResponse;
import com.devops.app.dto.UpdateStatusRequest;
import com.devops.app.config.TestSecurityConfig;
import com.devops.app.exception.GlobalExceptionHandler;
import com.devops.app.exception.TaskNotFoundException;
import com.devops.app.model.Task;
import com.devops.app.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import({TestSecurityConfig.class, GlobalExceptionHandler.class})
class TaskControllerPatchTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean TaskService taskService;

    @Test
    void patchStatusReturns200() throws Exception {
        TaskResponse resp = new TaskResponse(1L, "Task", "desc",
            Task.TaskStatus.DONE, Task.Priority.LOW, LocalDateTime.now(), LocalDateTime.now());
        when(taskService.updateStatus(eq(1L), eq(Task.TaskStatus.DONE))).thenReturn(resp);

        UpdateStatusRequest req = new UpdateStatusRequest(Task.TaskStatus.DONE);
        mockMvc.perform(patch("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DONE"));
    }

    @Test
    void patchStatusReturns404ForMissingTask() throws Exception {
        when(taskService.updateStatus(eq(99L), any())).thenThrow(new TaskNotFoundException(99L));

        UpdateStatusRequest req = new UpdateStatusRequest(Task.TaskStatus.DONE);
        mockMvc.perform(patch("/api/tasks/99/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
            .andExpect(status().isNotFound());
    }

    @Test
    void patchStatusReturns400ForNullStatus() throws Exception {
        mockMvc.perform(patch("/api/tasks/1/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":null}"))
            .andExpect(status().isBadRequest());
    }
}
