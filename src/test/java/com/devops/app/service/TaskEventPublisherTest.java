package com.devops.app.service;

import com.devops.app.model.Task;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TaskEventPublisherTest {

    private MeterRegistry registry;
    private TaskEventPublisher publisher;

    @BeforeEach
    void setUp() {
        registry = new SimpleMeterRegistry();
        publisher = new TaskEventPublisher(registry);
    }

    @Test
    void onTaskCreatedIncrementsCounter() {
        Task task = new Task("Test", "desc", Task.TaskStatus.TODO, Task.Priority.LOW);
        task.setId(1L);

        publisher.onTaskCreated(task);
        publisher.onTaskCreated(task);

        double count = registry.counter("tasks.created.total").count();
        assertThat(count).isEqualTo(2.0);
    }

    @Test
    void onTaskCompletedIncrementsCounter() {
        publisher.onTaskCompleted(1L);

        double count = registry.counter("tasks.completed.total").count();
        assertThat(count).isEqualTo(1.0);
    }

    @Test
    void onTaskDeletedIncrementsCounter() {
        publisher.onTaskDeleted(1L);
        publisher.onTaskDeleted(2L);
        publisher.onTaskDeleted(3L);

        double count = registry.counter("tasks.deleted.total").count();
        assertThat(count).isEqualTo(3.0);
    }
}
