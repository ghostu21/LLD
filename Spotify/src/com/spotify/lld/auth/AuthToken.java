package com.spotify.lld.auth;

import java.time.Instant;
import java.util.UUID;

public class AuthToken {
    private final String token;
    private final String userId;
    private final Instant expiresAt;

    public AuthToken(String userId) {
        this.token = UUID.randomUUID().toString();
        this.userId = userId;
        this.expiresAt = Instant.now().plusSeconds(3600);
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public Instant getExpiresAt() { return expiresAt; }
}
