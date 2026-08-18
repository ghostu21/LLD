package com.reco.lld.account;

import java.time.Instant;

/**
 * Short-lived bearer session issued after login.
 * <p>
 * Why: recommendation APIs must not accept a raw userId from the client as
 * proof of identity — that is a classic IDOR. The token is opaque (UUID)
 * and expires.
 */
public final class Session {
    private final String token;
    private final String userId;
    private final Instant expiresAt;

    public Session(String token, String userId, Instant expiresAt) {
        this.token = token;
        this.userId = userId;
        this.expiresAt = expiresAt;
    }

    public String getToken() { return token; }

    public String getUserId() { return userId; }

    public Instant getExpiresAt() { return expiresAt; }

    public boolean isExpired(Instant now) {
        return !now.isBefore(expiresAt);
    }
}
