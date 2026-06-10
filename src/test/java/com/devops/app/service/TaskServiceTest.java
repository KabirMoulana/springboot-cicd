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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService unit tests")
class TaskServiceTest {

    @Mock TaskRepository taskRepository;
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
        @DisplayName("returns TaskResponse when task exists")
        void returnsResponseWhenExists() {
            when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
            TaskResponse response = taskService.findById(1L);
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.title()).isEqualTo("Test Task");
        }

        @Test
        @DisplayName("throws TaskNotFoundException when task missing")
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
        @DisplayName("saves and returns new task")
        void savesAndReturns() {
            TaskRequest req = new TaskRequest("New Task", "Desc", Task.TaskStatus.TODO, Task.Priority.HIGH);
            when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);
            TaskResponse response = taskService.create(req);
            assertThat(response).isNotNull();
            verify(taskRepository, times(1)).save(any(Task.class));
        }
    }

    @Nested
    @DisplayName("update")
    class Update {
        @Test
        @DisplayName("updates existing task fields")
        void updatesFields() {
            TaskRequest req = new TaskRequest("Updated", "New desc", Task.TaskStatus.DONE, Task.Priority.LOW);
            when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
            when(taskRepository.save(any(Task.class))).thenAnswer(inv -> inv.getArgument(0));
            TaskResponse response = taskService.update(1L, req);
            assertThat(response.title()).isEqualTo("Updated");
            assertThat(response.status()).isEqualTo(Task.TaskStatus.DONE);
        }

        @Test
        @DisplayName("throws TaskNotFoundException when updating missing task")
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
        @DisplayName("deletes existing task")
        void deletesExisting() {
            when(taskRepository.existsById(1L)).thenReturn(true);
            taskService.delete(1L);
            verify(taskRepository).deleteById(1L);
        }

        @Test
        @DisplayName("throws when deleting missing task")
        void throwsWhenMissing() {
            when(taskRepository.existsById(99L)).thenReturn(false);
            assertThatThrownBy(() -> taskService.delete(99L))
                .isInstanceOf(TaskNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {
        @Test
        @DisplayName("returns paged response")
        void returnsPagedResponse() {
            PageRequest pageable = PageRequest.of(0, 10);
            when(taskRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(sampleTask)));
            var result = taskService.findAll(pageable);
            assertThat(result.content()).hasSize(1);
            assertThat(result.totalElements()).isEqualTo(1);
        }
    }
}
