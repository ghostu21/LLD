package com.gdrive.lld.account;

import com.gdrive.lld.events.DriveObserver;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Authenticated principal. Identity is {@code username} (map-safe).
 */
public final class User implements DriveObserver {
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final Role role;
    private final Set<Group> groups = ConcurrentHashMap.newKeySet();

    public User(String username, String passwordHash, String salt, Role role) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.salt = salt;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }

    public Set<Group> getGroups() {
        return Collections.unmodifiableSet(groups);
    }

    void addGroup(Group group) {
        groups.add(group);
    }

    void removeGroup(Group group) {
        groups.remove(group);
    }

    public boolean authenticate(String password) {
        return passwordHash.equals(PasswordUtils.hash(password, salt));
    }

    @Override
    public void update(String message) {
        System.out.println("Notify " + username + ": " + message);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User)) {
            return false;
        }
        return username.equals(((User) o).username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username);
    }
}
