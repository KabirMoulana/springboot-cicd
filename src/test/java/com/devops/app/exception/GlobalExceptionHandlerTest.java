package com.devops.app.exception;

import com.devops.app.config.SecurityConfig;
import com.devops.app.controller.TaskController;
import com.devops.app.service.TaskService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import({SecurityConfig.class, GlobalExceptionHandler.class})
@DisplayName("GlobalExceptionHandler tests")
class GlobalExceptionHandlerTest {

    @Autowired MockMvc mockMvc;
    @MockBean TaskService taskService;

    @Test
    @DisplayName("TaskNotFoundException returns RFC 9457 ProblemDetail with taskId")
    void taskNotFoundReturnsProblemDetail() throws Exception {
        when(taskService.findById(42L)).thenThrow(new TaskNotFoundException(42L));

        mockMvc.perform(get("/api/tasks/42"))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
            .andExpect(jsonPath("$.title").value("Task Not Found"))
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.taskId").value(42))
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Validation error returns 400 with field errors map")
    void validationErrorReturns400() throws Exception {
        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"title\":\"\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.title").value("Validation Error"))
            .andExpect(jsonPath("$.errors").exists())
            .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    @DisplayName("Generic exception returns 500 without leaking internal details")
    void genericExceptionReturns500() throws Exception {
        when(taskService.findById(1L)).thenThrow(new RuntimeException("Internal DB error"));

        mockMvc.perform(get("/api/tasks/1"))
            .andExpect(status().isInternalServerError())
            .andExpect(jsonPath("$.title").value("Internal Server Error"))
            .andExpect(jsonPath("$.detail").value("An unexpected error occurred"));
    }
}
