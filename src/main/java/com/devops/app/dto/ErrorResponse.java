package com.devops.app.dto;

import java.time.Instant;
import java.util.Map;

/**
 * Supplementary error response DTO for non-ProblemDetail scenarios.
 */
public record ErrorResponse(
    int status,
    String title,
    String detail,
    Instant timestamp,
    Map<String, String> errors
) {
    public static ErrorResponse of(int status, String title, String detail) {
        return new ErrorResponse(status, title, detail, Instant.now(), null);
    }

    public static ErrorResponse withErrors(int status, String title, String detail,
                                           Map<String, String> errors) {
        return new ErrorResponse(status, title, detail, Instant.now(), errors);
    }
}
