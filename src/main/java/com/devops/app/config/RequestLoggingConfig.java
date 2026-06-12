package com.devops.app.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Configuration
public class RequestLoggingConfig {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingConfig.class);

    @Bean("requestLoggingFilter")
    public OncePerRequestFilter requestLoggingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain) throws ServletException, IOException {
                String requestId = request.getHeader("X-Request-ID");
                if (requestId == null || requestId.isBlank()) {
                    requestId = UUID.randomUUID().toString().substring(0, 8);
                }

                MDC.put("requestId", requestId);
                MDC.put("method", request.getMethod());
                MDC.put("uri", request.getRequestURI());

                response.setHeader("X-Request-ID", requestId);

                long start = System.currentTimeMillis();
                try {
                    chain.doFilter(request, response);
                } finally {
                    long duration = System.currentTimeMillis() - start;
                    // Skip actuator noise
                    if (!request.getRequestURI().startsWith("/actuator")) {
                        log.info("{} {} → {} ({}ms)",
                            request.getMethod(),
                            request.getRequestURI(),
                            response.getStatus(),
                            duration);
                    }
                    MDC.clear();
                }
            }
        };
    }
}
