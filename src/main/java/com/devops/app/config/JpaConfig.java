package com.devops.app.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
    // Spring Boot auto-configures repositories and transactions.
    // Only JPA Auditing needs explicit enabling here.
}
