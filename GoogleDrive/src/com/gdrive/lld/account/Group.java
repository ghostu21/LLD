package com.gdrive.lld.account;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Named set of users for folder/file sharing.
 */
public final class Group {
    private final String name;
    private final Set<User> members = ConcurrentHashMap.newKeySet();

    public Group(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void addUser(User user) {
        members.add(user);
        user.addGroup(this);
    }

    public void removeUser(User user) {
        members.remove(user);
        user.removeGroup(this);
    }

    public Set<User> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Group)) {
            return false;
        }
        return name.equals(((Group) o).name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }
}
