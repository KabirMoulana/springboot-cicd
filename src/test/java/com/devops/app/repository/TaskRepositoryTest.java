package com.devops.app.repository;

import com.devops.app.model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("TaskRepository JPA tests")
class TaskRepositoryTest {

    @Autowired TaskRepository taskRepository;

    @BeforeEach
    void setUp() {
        taskRepository.deleteAll();
        taskRepository.saveAll(List.of(
            new Task("Task A", "desc", Task.TaskStatus.TODO, Task.Priority.HIGH),
            new Task("Task B", "desc", Task.TaskStatus.DONE, Task.Priority.LOW),
            new Task("Task C", "desc", Task.TaskStatus.TODO, Task.Priority.MEDIUM),
            new Task("Task D", "desc", Task.TaskStatus.IN_PROGRESS, Task.Priority.CRITICAL)
        ));
    }

    @Test
    void findByStatusReturnsMatchingTasks() {
        Page<Task> todos = taskRepository.findByStatus(Task.TaskStatus.TODO, PageRequest.of(0, 10));
        assertThat(todos.getTotalElements()).isEqualTo(2);
    }

    @Test
    void searchByTitleFiltersTasks() {
        Page<Task> results = taskRepository.search("Task A", null, null, PageRequest.of(0, 10));
        assertThat(results.getTotalElements()).isEqualTo(1);
        assertThat(results.getContent().get(0).getTitle()).isEqualTo("Task A");
    }

    @Test
    void searchIsCaseInsensitive() {
        Page<Task> results = taskRepository.search("task", null, null, PageRequest.of(0, 10));
        assertThat(results.getTotalElements()).isEqualTo(4);
    }

    @Test
    void countByStatusReturnsCorrectCount() {
        long todoCount = taskRepository.countByStatus(Task.TaskStatus.TODO);
        assertThat(todoCount).isEqualTo(2);
    }

    @Test
    void findByStatusAndPriorityFiltersCorrectly() {
        Page<Task> results = taskRepository.findByStatusAndPriority(
            Task.TaskStatus.TODO, Task.Priority.HIGH, PageRequest.of(0, 10));
        assertThat(results.getTotalElements()).isEqualTo(1);
    }

    @Test
    void countByStatusGroupedReturnsAllStatuses() {
        List<Object[]> counts = taskRepository.countByStatus();
        assertThat(counts).isNotEmpty();
    }
}
