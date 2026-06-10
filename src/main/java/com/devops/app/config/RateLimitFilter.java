package com.devops.app.config;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Configuration
public class RateLimitFilter {

    private static final Logger log = LoggerFactory.getLogger(RateLimitFilter.class);

    // Simple in-process per-IP rate limiter (replace with Redis in production)
    private final ConcurrentHashMap<String, RateLimiter> limiters = new ConcurrentHashMap<>();

    @Bean
    @ConditionalOnProperty(name = "app.rate-limit.enabled", havingValue = "true", matchIfMissing = false)
    public OncePerRequestFilter rateLimitingFilter() {
        return new OncePerRequestFilter() {
            @Override
            protected void doFilterInternal(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain) throws ServletException, IOException {
                // Only rate-limit API endpoints
                if (!request.getRequestURI().startsWith("/api/")) {
                    chain.doFilter(request, response);
                    return;
                }

                String clientIp = getClientIp(request);
                RateLimiter limiter = limiters.computeIfAbsent(clientIp,
                    k -> new RateLimiter(100, 60_000)); // 100 req/min per IP

                if (!limiter.tryAcquire()) {
                    log.warn("Rate limit exceeded for IP={}", clientIp);
                    response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                    response.setContentType("application/json");
                    response.setHeader("Retry-After", "60");
                    response.getWriter().write("""
                        {"status":429,"title":"Too Many Requests","detail":"Rate limit exceeded. Try again in 60 seconds."}
                        """);
                    return;
                }
                chain.doFilter(request, response);
            }

            private String getClientIp(HttpServletRequest req) {
                String forwarded = req.getHeader("X-Forwarded-For");
                return (forwarded != null) ? forwarded.split(",")[0].trim() : req.getRemoteAddr();
            }
        };
    }

    static class RateLimiter {
        private final int limit;
        private final long windowMs;
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = Instant.now().toEpochMilli();

        RateLimiter(int limit, long windowMs) {
            this.limit = limit;
            this.windowMs = windowMs;
        }

        boolean tryAcquire() {
            long now = Instant.now().toEpochMilli();
            if (now - windowStart > windowMs) {
                windowStart = now;
                count.set(0);
            }
            return count.incrementAndGet() <= limit;
        }
    }
}
