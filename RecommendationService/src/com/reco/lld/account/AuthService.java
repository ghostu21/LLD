package com.reco.lld.account;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registration, login, and session validation.
 * <p>
 * Why: personalized ranking is a privacy-sensitive API. Callers prove
 * identity with a session token, not a client-supplied user id.
 * <p>
 * Logic: register hashes the password before persist; login verifies with
 * constant-time compare and issues a TTL session; {@link #requireUser}
 * rejects missing/expired tokens.
 */
public class AuthService {
    private static final Duration SESSION_TTL = Duration.ofHours(1);

    private final UserStore users;
    private final ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    public AuthService(UserStore users) {
        this.users = users;
    }

    public User register(String username, String password, UserRole role, String email) throws Exception {
        if (username == null || username.isBlank() || password == null || password.length() < 8) {
            throw new IllegalArgumentException("Username required; password must be at least 8 characters");
        }
        if (users.findByUsername(username) != null) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (role == UserRole.GUEST) {
            throw new IllegalArgumentException("Guests are not registered accounts");
        }
        String salt = PasswordUtils.generateSalt();
        String hash = PasswordUtils.hash(password, salt);
        User user = new User(username, hash, salt, role, email);
        users.save(user);
        return user;
    }

    public Session login(String username, String password) throws Exception {
        User user = users.findByUsername(username);
        if (user == null || user.getStatus() != AccountStatus.ACTIVE || !user.verifyPassword(password)) {
            throw new AuthenticationException("Invalid username or password");
        }
        Session session = new Session(UUID.randomUUID().toString(), user.getUserId(),
                Instant.now().plus(SESSION_TTL));
        sessions.put(session.getToken(), session);
        return session;
    }

    public Session guestSession() {
        User guest = User.guest();
        users.save(guest);
        Session session = new Session(UUID.randomUUID().toString(), guest.getUserId(),
                Instant.now().plus(SESSION_TTL));
        sessions.put(session.getToken(), session);
        return session;
    }

    public User requireUser(String token) {
        if (token == null || token.isBlank()) {
            throw new AuthenticationException("Missing session token");
        }
        Session session = sessions.get(token);
        if (session == null || session.isExpired(Instant.now())) {
            sessions.remove(token);
            throw new AuthenticationException("Invalid or expired session");
        }
        User user = users.findById(session.getUserId());
        if (user == null || user.getStatus() != AccountStatus.ACTIVE) {
            throw new AuthenticationException("Account is not active");
        }
        return user;
    }

    public void logout(String token) {
        if (token != null) sessions.remove(token);
    }

    public UserStore getUsers() {
        return users;
    }
}
