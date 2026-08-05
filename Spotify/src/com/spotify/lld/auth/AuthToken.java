package com.spotify.lld.auth;

import java.time.Instant;
import java.util.UUID;

/**
 * Short-lived opaque session credential issued after a successful login.
 * <p>
 * Why: clients present this token on API calls instead of the password.
 * Expiry limits damage if a token is stolen.
 * <p>
 * Logic: token string is a random UUID; bound to {@code userId}; default TTL
 * is 1 hour from creation. {@link #isExpired} compares wall-clock now to
 * {@code expiresAt}.
 */
public class AuthToken {
    /** Opaque bearer value returned to the client. */
    private final String token;
    /** Account this session belongs to. */
    private final String userId;
    /** Absolute expiry instant; after this, AuthService rejects the token. */
    private final Instant expiresAt;

    /**
     * Issues a new token for {@code userId} valid for 3600 seconds.
     */
    public AuthToken(String userId) {
        this.token = UUID.randomUUID().toString();
        this.userId = userId;
        this.expiresAt = Instant.now().plusSeconds(3600);
    }

    /** @return true if current time is past {@code expiresAt}. */
    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public String getToken() { return token; }
    public String getUserId() { return userId; }
    public Instant getExpiresAt() { return expiresAt; }
}
