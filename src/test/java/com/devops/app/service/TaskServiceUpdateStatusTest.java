package com.devops.app.service;

import com.devops.app.dto.TaskResponse;
import com.devops.app.exception.TaskNotFoundException;
import com.devops.app.model.Task;
import com.devops.app.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceUpdateStatusTest {

    @Mock TaskRepository taskRepository;
    @Mock TaskEventPublisher eventPublisher;
    @Mock AuditService auditService;
    @InjectMocks TaskService taskService;

    private Task task;

    @BeforeEach
    void setUp() {
        task = new Task("Task", "desc", Task.TaskStatus.TODO, Task.Priority.MEDIUM);
        task.setId(5L);
    }

    @Test
    void updateStatusToDonePublishesCompletedEvent() {
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TaskResponse response = taskService.updateStatus(5L, Task.TaskStatus.DONE);

        assertThat(response.status()).isEqualTo(Task.TaskStatus.DONE);
        verify(eventPublisher).onTaskCompleted(5L);
    }

    @Test
    void updateStatusToInProgressDoesNotPublishCompletedEvent() {
        when(taskRepository.findById(5L)).thenReturn(Optional.of(task));
        when(taskRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        taskService.updateStatus(5L, Task.TaskStatus.IN_PROGRESS);

        verify(eventPublisher, never()).onTaskCompleted(anyLong());
    }

    @Test
    void updateStatusThrowsForMissingTask() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.updateStatus(99L, Task.TaskStatus.DONE))
            .isInstanceOf(TaskNotFoundException.class);
    }
}
