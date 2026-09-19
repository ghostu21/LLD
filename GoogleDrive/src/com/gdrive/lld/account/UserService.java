package com.gdrive.lld.account;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Register / login. Passwords are salted hashes, never stored in the clear.
 */
public final class UserService {
    private final Map<String, User> users = new ConcurrentHashMap<>();

    public User register(String username, String password, Role role) {
        String salt = PasswordUtils.generateSalt();
        User user = new User(username, PasswordUtils.hash(password, salt), salt, role);
        User previous = users.putIfAbsent(username, user);
        if (previous != null) {
            throw new IllegalArgumentException("User already exists: " + username);
        }
        return user;
    }

    public User login(String username, String password) {
        User user = users.get(username);
        if (user == null || !user.authenticate(password)) {
            throw new SecurityException("Invalid credentials");
        }
        return user;
    }

    public User get(String username) {
        return users.get(username);
    }
}
