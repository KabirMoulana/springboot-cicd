package com.devops.app;

import com.devops.app.config.TestAsyncConfig;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@Import(TestAsyncConfig.class)
class ApplicationContextTest {

    @Test
    void contextLoads() {
        // Verifies full Spring context starts without error
    }
}
