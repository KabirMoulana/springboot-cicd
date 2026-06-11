package com.devops.app.service;

import com.devops.app.dto.TaskResponse;
import com.devops.app.model.Task;
import com.devops.app.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import com.devops.app.config.TestAsyncConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestAsyncConfig.class)
class TaskServiceCacheTest {

    @Autowired TaskService taskService;
    @Autowired CacheManager cacheManager;
    @MockBean TaskRepository taskRepository;
    @MockBean AuditService auditService;

    @BeforeEach
    void clearCache() {
        cacheManager.getCache("tasks").clear();
    }

    @Test
    void findByIdShouldCacheResult() {
        Task task = new Task("Cached Task", "desc", Task.TaskStatus.TODO, Task.Priority.LOW);
        task.setId(100L);
        when(taskRepository.findById(100L)).thenReturn(Optional.of(task));

        // First call — hits DB
        TaskResponse r1 = taskService.findById(100L);
        // Second call — should use cache, repo NOT called again
        TaskResponse r2 = taskService.findById(100L);

        assertThat(r1.id()).isEqualTo(r2.id());
        verify(taskRepository, times(1)).findById(100L);
    }

    @Test
    void createShouldEvictAllCacheEntries() {
        Task existing = new Task("Existing", "desc", Task.TaskStatus.TODO, Task.Priority.LOW);
        existing.setId(1L);
        when(taskRepository.findById(1L)).thenReturn(Optional.of(existing));

        // Populate cache
        taskService.findById(1L);

        // Create evicts all
        Task newTask = new Task("New", "desc", Task.TaskStatus.TODO, Task.Priority.MEDIUM);
        newTask.setId(2L);
        when(taskRepository.save(any())).thenReturn(newTask);
        taskService.create(new com.devops.app.dto.TaskRequest("New", null,
            Task.TaskStatus.TODO, Task.Priority.MEDIUM));

        // Should hit repo again after cache eviction
        taskService.findById(1L);
        verify(taskRepository, times(2)).findById(1L);
    }
}
