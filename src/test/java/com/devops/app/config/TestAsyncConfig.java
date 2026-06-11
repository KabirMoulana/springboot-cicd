package com.devops.app.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SyncTaskExecutor;

import java.util.concurrent.Executor;

/**
 * Replaces async executor with synchronous one in tests.
 * This makes @Async methods execute on the calling thread,
 * making audit log writes visible immediately without Awaitility.
 */
@TestConfiguration
public class TestAsyncConfig {

    @Bean(name = "taskExecutor")
    public Executor syncExecutor() {
        return new SyncTaskExecutor();
    }
}
