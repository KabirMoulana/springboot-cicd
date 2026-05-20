package com.devops.app;

import com.devops.app.controller.TaskController;
import com.devops.app.model.Task;
import com.devops.app.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
class TaskControllerTest {

    @Autowired MockMvc mockMvc;
    @Autowired ObjectMapper objectMapper;
    @MockBean TaskService taskService;

    @Test
    void rootEndpointReturnsAppInfo() throws Exception {
        mockMvc.perform(get("/api/"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.app").value("springboot-cicd"));
    }

    @Test
    void getAllTasksReturnsListSuccessfully() throws Exception {
        when(taskService.findAll()).thenReturn(List.of(
            new Task(1L, "Write tests", "TODO"),
            new Task(2L, "Deploy app", "IN_PROGRESS")
        ));

        mockMvc.perform(get("/api/tasks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].title").value("Write tests"));
    }

    @Test
    void getTaskByIdReturns404WhenNotFound() throws Exception {
        when(taskService.findById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/tasks/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createTaskPersistsAndReturnsTask() throws Exception {
        Task input = new Task(null, "New Task", "TODO");
        Task saved = new Task(1L, "New Task", "TODO");

        when(taskService.save(any(Task.class))).thenReturn(saved);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(input)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.title").value("New Task"));
    }

    @Test
    void deleteTaskReturns204OnSuccess() throws Exception {
        when(taskService.deleteById(1L)).thenReturn(true);

        mockMvc.perform(delete("/api/tasks/1"))
            .andExpect(status().isNoContent());
    }
}
