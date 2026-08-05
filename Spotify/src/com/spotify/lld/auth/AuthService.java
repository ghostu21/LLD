package com.spotify.lld.auth;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class AuthService {
    private final Map<String, AuthToken> tokenStore = new ConcurrentHashMap<>();
    private final Map<String, User> userStore = new ConcurrentHashMap<>();

    public void register(User user) {
        userStore.put(user.getUsername(), user);
    }

    public AuthToken login(String username, String plainPassword) throws Exception {
        User user = userStore.get(username);
        if (user == null || !user.checkPassword(plainPassword)) {
            throw new SecurityException("Invalid credentials.");
        }
        AuthToken token = new AuthToken(user.getUserId());
        tokenStore.put(token.getToken(), token);
        return token;
    }

    public Optional<String> validateToken(String token) {
        AuthToken t = tokenStore.get(token);
        if (t == null || t.isExpired()) {
            tokenStore.remove(token);
            return Optional.empty();
        }
        return Optional.of(t.getUserId());
    }

    public void logout(String token) {
        tokenStore.remove(token);
    }
}
