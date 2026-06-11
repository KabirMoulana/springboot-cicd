package com.devops.app.config;

import com.devops.app.model.Task;
import com.devops.app.repository.TaskRepository;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
public class MetricsConfig {

    private static final Logger log = LoggerFactory.getLogger(MetricsConfig.class);

    private final MeterRegistry meterRegistry;
    private final TaskRepository taskRepository;

    public MetricsConfig(MeterRegistry meterRegistry, TaskRepository taskRepository) {
        this.meterRegistry = meterRegistry;
        this.taskRepository = taskRepository;
    }

    // Use ApplicationReadyEvent instead of @PostConstruct so JPA/DB is fully ready
    @EventListener(ApplicationReadyEvent.class)
    public void registerMetrics() {
        try {
            for (Task.TaskStatus status : Task.TaskStatus.values()) {
                Gauge.builder("tasks.count", taskRepository,
                        repo -> safeCount(repo, status))
                    .tag("status", status.name().toLowerCase())
                    .description("Number of tasks by status")
                    .register(meterRegistry);
            }
            Gauge.builder("tasks.total", taskRepository,
                    repo -> (double) safeTotal(repo))
                .description("Total number of tasks")
                .register(meterRegistry);
        } catch (Exception e) {
            log.warn("Failed to register task metrics gauges: {}", e.getMessage());
        }
    }

    private double safeCount(TaskRepository repo, Task.TaskStatus status) {
        try {
            return (double) repo.countByStatus(status);
        } catch (Exception e) {
            return 0;
        }
    }

    private long safeTotal(TaskRepository repo) {
        try {
            return repo.count();
        } catch (Exception e) {
            return 0;
        }
    }
}
