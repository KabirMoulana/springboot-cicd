package com.devops.app.config;

import com.devops.app.repository.TaskRepository;
import com.devops.app.model.Task;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    private final MeterRegistry meterRegistry;
    private final TaskRepository taskRepository;

    public MetricsConfig(MeterRegistry meterRegistry, TaskRepository taskRepository) {
        this.meterRegistry = meterRegistry;
        this.taskRepository = taskRepository;
    }

    @PostConstruct
    public void registerMetrics() {
        for (Task.TaskStatus status : Task.TaskStatus.values()) {
            Gauge.builder("tasks.count", taskRepository, repo -> repo.countByStatus(status))
                .tag("status", status.name().toLowerCase())
                .description("Number of tasks by status")
                .register(meterRegistry);
        }

        Gauge.builder("tasks.total", taskRepository, repo -> (double) repo.count())
            .description("Total number of tasks")
            .register(meterRegistry);
    }
}
