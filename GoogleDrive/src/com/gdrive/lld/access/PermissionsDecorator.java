package com.gdrive.lld.access;

import com.gdrive.lld.account.Group;
import com.gdrive.lld.account.User;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-node ACL (Decorator): user and group grants on one file or folder.
 * <p>
 * Why: Drive permissions are not only global roles — each node has its own
 * map, and an explicit {@link AccessLevel#NONE} overrides inherited access.
 */
public final class PermissionsDecorator {
    private final ConcurrentHashMap<String, AccessLevel> userPermissions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AccessLevel> groupPermissions = new ConcurrentHashMap<>();

    public void setPermission(User user, AccessLevel accessLevel) {
        userPermissions.put(user.getUsername(), accessLevel);
    }

    public void setPermission(Group group, AccessLevel accessLevel) {
        groupPermissions.put(group.getName(), accessLevel);
    }

    /**
     * Direct grant on this node only (user beats group; empty if inherit).
     */
    public Optional<AccessLevel> findDirect(User user) {
        AccessLevel userLevel = userPermissions.get(user.getUsername());
        if (userLevel != null) {
            return Optional.of(userLevel);
        }
        AccessLevel bestGroup = null;
        for (Group group : user.getGroups()) {
            AccessLevel groupLevel = groupPermissions.get(group.getName());
            if (groupLevel != null && (bestGroup == null || groupLevel.ordinal() < bestGroup.ordinal())) {
                bestGroup = groupLevel;
            }
        }
        return Optional.ofNullable(bestGroup);
    }
}
