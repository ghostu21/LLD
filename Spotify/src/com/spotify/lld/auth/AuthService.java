package com.spotify.lld.auth;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Facade for registration, login, and session-token lifecycle.
 * <p>
 * Why: APIs must authenticate with short-lived tokens, never by re-sending
 * passwords. Keeps user store and token store behind one service boundary.
 * <p>
 * Logic: register stores users by username; login verifies the password hash
 * then issues an {@link AuthToken}; validateToken checks existence + expiry;
 * logout revokes by deleting the token entry.
 */
public class AuthService {
    /** Active session tokens keyed by opaque token string. */
    private final Map<String, AuthToken> tokenStore = new ConcurrentHashMap<>();
    /** Registered users keyed by username. */
    private final Map<String, User> userStore = new ConcurrentHashMap<>();

    /**
     * Persists a new user in the in-memory store (demo only).
     * Does not issue a token — caller must {@link #login} after register.
     */
    public void register(User user) {
        userStore.put(user.getUsername(), user);
    }

    /**
     * Verifies credentials and creates a session token.
     * <p>
     * Logic: lookup user → reject if missing or password mismatch →
     * create {@link AuthToken} bound to userId → store and return it.
     *
     * @throws SecurityException if username/password are invalid
     */
    public AuthToken login(String username, String plainPassword) throws Exception {
        User user = userStore.get(username);
        if (user == null || !user.checkPassword(plainPassword)) {
            throw new SecurityException("Invalid credentials.");
        }
        AuthToken token = new AuthToken(user.getUserId());
        tokenStore.put(token.getToken(), token);
        return token;
    }

    /**
     * Resolves a bearer token to a userId if it is still valid.
     * <p>
     * Logic: missing or expired tokens are removed (lazy cleanup) and
     * return empty; otherwise returns the owning userId.
     */
    public Optional<String> validateToken(String token) {
        AuthToken t = tokenStore.get(token);
        if (t == null || t.isExpired()) {
            tokenStore.remove(token);
            return Optional.empty();
        }
        return Optional.of(t.getUserId());
    }

    /**
     * Explicit logout: immediately invalidates the token so it cannot be reused.
     */
    public void logout(String token) {
        tokenStore.remove(token);
    }
}
