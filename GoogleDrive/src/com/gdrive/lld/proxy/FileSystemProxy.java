package com.gdrive.lld.proxy;

import com.gdrive.lld.access.AccessDeniedException;
import com.gdrive.lld.access.AccessLevel;
import com.gdrive.lld.account.Role;
import com.gdrive.lld.account.User;
import com.gdrive.lld.fs.FileSystemComponent;
import com.gdrive.lld.fs.Folder;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Proxy that enforces ACL before every mutation / read.
 * <p>
 * Why: keep permission checks out of File/Folder so the composite stays
 * about tree shape. Effective access walks parents; explicit NONE wins.
 */
public final class FileSystemProxy implements FileSystemComponent {
    private final FileSystemComponent realComponent;
    private final User currentUser;

    public FileSystemProxy(FileSystemComponent component, User user) {
        this.realComponent = component;
        this.currentUser = user;
    }

    public FileSystemComponent unwrap() {
        return realComponent;
    }

    public AccessLevel effectiveAccess() {
        return getEffectiveAccessLevel();
    }

    @Override
    public String getId() {
        return realComponent.getId();
    }

    @Override
    public String getName() {
        synchronized (realComponent) {
            return realComponent.getName();
        }
    }

    @Override
    public void setName(String name) {
        synchronized (realComponent) {
            require(AccessLevel.EDITOR);
            realComponent.setName(name);
        }
    }

    @Override
    public Folder getParentFolder() {
        return realComponent.getParentFolder();
    }

    @Override
    public void setParentFolder(Folder parent) {
        require(AccessLevel.EDITOR);
        realComponent.setParentFolder(parent);
    }

    @Override
    public com.gdrive.lld.access.PermissionsDecorator getPermissions() {
        return realComponent.getPermissions();
    }

    @Override
    public ReentrantLock getLock() {
        return realComponent.getLock();
    }

    @Override
    public void add(FileSystemComponent component) {
        synchronized (realComponent) {
            require(AccessLevel.EDITOR);
            realComponent.add(component);
        }
    }

    @Override
    public void remove(FileSystemComponent component) {
        synchronized (realComponent) {
            require(AccessLevel.EDITOR);
            realComponent.remove(component);
        }
    }

    @Override
    public FileSystemComponent getChild(String name) {
        synchronized (realComponent) {
            require(AccessLevel.VIEWER);
            FileSystemComponent child = realComponent.getChild(name);
            if (child == null) {
                return null;
            }
            return new FileSystemProxy(child, currentUser);
        }
    }

    @Override
    public void display(String indent) {
        synchronized (realComponent) {
            require(AccessLevel.VIEWER);
            realComponent.display(indent);
        }
    }

    public void require(AccessLevel required) {
        AccessLevel actual = getEffectiveAccessLevel();
        if (!actual.allows(required)) {
            throw new AccessDeniedException(
                    "No " + required + " permission for user: " + currentUser.getUsername()
                            + " (effective=" + actual + ") on " + realComponent.getName());
        }
    }

    private AccessLevel getEffectiveAccessLevel() {
        if (currentUser.getRole() == Role.ADMIN) {
            return AccessLevel.OWNER;
        }
        FileSystemComponent component = realComponent;
        while (component != null) {
            Optional<AccessLevel> direct = component.getPermissions().findDirect(currentUser);
            if (direct.isPresent()) {
                return direct.get();
            }
            component = component.getParentFolder();
        }
        return AccessLevel.NONE;
    }
}
