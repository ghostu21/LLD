package com.reco.lld.account;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory user directory (stand-in for a user-service DB).
 * <p>
 * Why: ranking never talks to a raw map of users from controllers —
 * lookup is centralized so blocked accounts are enforced everywhere.
 */
public class UserStore {
    private final ConcurrentHashMap<String, User> byId = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, User> byUsername = new ConcurrentHashMap<>();

    public void save(User user) {
        byId.put(user.getUserId(), user);
        byUsername.put(user.getUsername(), user);
    }

    public User findById(String userId) {
        return byId.get(userId);
    }

    public User findByUsername(String username) {
        return byUsername.get(username);
    }

    public Collection<User> all() {
        return byId.values();
    }
}
