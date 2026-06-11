package com.devops.app.dto;

import java.time.Instant;
import java.util.Set;

public record AuthResponse(
    String accessToken,
    String tokenType,
    long expiresIn,
    String username,
    Set<String> roles,
    Instant issuedAt
) {
    public static AuthResponse of(String token, long expiresIn, String username, Set<String> roles) {
        return new AuthResponse(token, "Bearer", expiresIn, username, roles, Instant.now());
    }
}
