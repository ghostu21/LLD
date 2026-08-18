package com.reco.lld.account;

import java.util.UUID;

/**
 * Authenticated identity. Passwords are stored as salt + hash only.
 * <p>
 * Why: recommendation personalization is keyed by user id, but the ranking
 * payload must never include credentials, email, or other users' PII.
 */
public class User {
    private final String userId;
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final UserRole role;
    private final String email;
    private AccountStatus status;

    public User(String username, String passwordHash, String salt, UserRole role, String email) {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
        this.email = email;
        this.status = AccountStatus.ACTIVE;
    }

    /** Ephemeral guest — no credentials, popularity-only ranking. */
    public static User guest() {
        User g = new User("guest-" + UUID.randomUUID(), null, null, UserRole.GUEST, null);
        return g;
    }

    public String getUserId() { return userId; }

    public String getUsername() { return username; }

    public UserRole getRole() { return role; }

    /** Contact email — never copied into recommendation responses. */
    public String getEmail() { return email; }

    public AccountStatus getStatus() { return status; }

    public void setStatus(AccountStatus status) { this.status = status; }

    public boolean verifyPassword(String candidate) throws Exception {
        if (role == UserRole.GUEST || passwordHash == null) return false;
        return PasswordUtils.verify(candidate, salt, passwordHash);
    }

    @Override
    public String toString() {
        return "User{id=" + userId + ", username=" + username + ", role=" + role + "}";
    }
}
