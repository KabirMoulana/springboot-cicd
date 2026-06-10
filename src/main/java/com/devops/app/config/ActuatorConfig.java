package com.devops.app.config;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import com.devops.app.repository.TaskRepository;

@Configuration
public class ActuatorConfig {

    @Bean
    public HealthIndicator taskDbHealthIndicator(TaskRepository taskRepository) {
        return () -> {
            try {
                long count = taskRepository.count();
                return Health.up()
                    .withDetail("taskCount", count)
                    .withDetail("status", "database reachable")
                    .build();
            } catch (Exception e) {
                return Health.down()
                    .withException(e)
                    .build();
            }
        };
    }
}
