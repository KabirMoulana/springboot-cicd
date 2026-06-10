package com.devops.app.service;

import com.devops.app.dto.TaskResponse;
import com.devops.app.model.Task;
import com.devops.app.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class TaskServiceCacheTest {

    @Autowired TaskService taskService;
    @SpyBean TaskRepository taskRepository;

    @Test
    void findByIdShouldCacheResult() {
        Task task = new Task("Cached Task", "desc", Task.TaskStatus.TODO, Task.Priority.LOW);
        task.setId(100L);
        doReturn(Optional.of(task)).when(taskRepository).findById(100L);

        // First call — hits DB
        TaskResponse r1 = taskService.findById(100L);
        // Second call — should use cache
        TaskResponse r2 = taskService.findById(100L);

        assertThat(r1.id()).isEqualTo(r2.id());
        // Repository should only be called once due to caching
        verify(taskRepository, times(1)).findById(100L);
    }
}
