package com.devops.app.service;

import com.devops.app.dto.TaskRequest;
import com.devops.app.dto.TaskResponse;
import com.devops.app.exception.TaskNotFoundException;
import com.devops.app.model.Task;
import com.devops.app.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService unit tests")
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
    @Mock TaskEventPublisher eventPublisher;
    @InjectMocks TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task("Test Task", "Description", Task.TaskStatus.TODO, Task.Priority.MEDIUM);
        sampleTask.setId(1L);
    }

    @Nested
    @DisplayName("findById")
    class FindById {
        @Test
        void returnsResponseWhenExists() {
            when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
            TaskResponse response = taskService.findById(1L);
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.title()).isEqualTo("Test Task");
        }

        @Test
        void throwsWhenMissing() {
            when(taskRepository.findById(99L)).thenReturn(Optional.empty());
            assertThatThrownBy(() -> taskService.findById(99L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("create")
    class Create {
        @Test
        void savesAndPublishesEvent() {
            TaskRequest req = new TaskRequest("New Task", "Desc", Task.TaskStatus.TODO, Task.Priority.HIGH);
            when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);
            TaskResponse response = taskService.create(req);
            assertThat(response).isNotNull();
            verify(taskRepository, times(1)).save(any(Task.class));
            verify(eventPublisher, times(1)).onTaskCreated(any(Task.class));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        void updatesAndPublishesCompletedEventWhenDone() {
            TaskRequest req = new TaskRequest("Updated", "New desc", Task.TaskStatus.DONE, Task.Priority.LOW);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
            taskService.update(1L, req);
            verify(eventPublisher, times(1)).onTaskCompleted(1L);
        }

        @Test
        void doesNotPublishCompletedWhenStatusNotDone() {
            TaskRequest req = new TaskRequest("Updated", "Desc", Task.TaskStatus.IN_PROGRESS, Task.Priority.LOW);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
            taskService.update(1L, req);
            verify(eventPublisher, never()).onTaskCompleted(anyLong());
        }

        @Test
        void throwsWhenMissing() {
            when(taskRepository.findById(99L)).thenReturn(Optional.empty());
            TaskRequest req = new TaskRequest("X", null, Task.TaskStatus.TODO, Task.Priority.LOW);
            assertThatThrownBy(() -> taskService.update(99L, req))
                .isInstanceOf(TaskNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("delete")
    class Delete {
        @Test
        void deletesAndPublishesEvent() {
            when(taskRepository.existsById(1L)).thenReturn(true);
            taskService.delete(1L);
            verify(taskRepository).deleteById(1L);
            verify(eventPublisher, times(1)).onTaskDeleted(1L);
        }

        @Test
        void throwsWhenMissing() {
            when(taskRepository.existsById(99L)).thenReturn(false);
            assertThatThrownBy(() -> taskService.delete(99L))
                .isInstanceOf(TaskNotFoundException.class);
            verify(eventPublisher, never()).onTaskDeleted(anyLong());
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {
        @Test
        void returnsPagedResponse() {
            PageRequest pageable = PageRequest.of(0, 10);
            when(taskRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(sampleTask)));
            var result = taskService.findAll(pageable);
            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
        }
    }
}
