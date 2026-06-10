package com.devops.app.service;

import com.devops.app.model.Task;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TaskEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(TaskEventPublisher.class);

    private final Counter taskCreatedCounter;
    private final Counter taskCompletedCounter;
    private final Counter taskDeletedCounter;

    public TaskEventPublisher(MeterRegistry registry) {
        this.taskCreatedCounter = Counter.builder("tasks.created.total")
            .description("Total number of tasks created")
            .register(registry);
        this.taskCompletedCounter = Counter.builder("tasks.completed.total")
            .description("Total number of tasks marked as DONE")
            .register(registry);
        this.taskDeletedCounter = Counter.builder("tasks.deleted.total")
            .description("Total number of tasks deleted")
            .register(registry);
    }

    public void onTaskCreated(Task task) {
        taskCreatedCounter.increment();
        log.info("EVENT task.created id={} priority={}", task.getId(), task.getPriority());
    }

    public void onTaskCompleted(Long taskId) {
        taskCompletedCounter.increment();
        log.info("EVENT task.completed id={}", taskId);
    }

    public void onTaskDeleted(Long taskId) {
        taskDeletedCounter.increment();
        log.info("EVENT task.deleted id={}", taskId);
    }
}
