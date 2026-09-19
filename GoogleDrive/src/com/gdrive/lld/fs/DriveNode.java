package com.gdrive.lld.fs;

import com.gdrive.lld.access.PermissionsDecorator;
import com.gdrive.lld.events.DriveObserver;
import com.gdrive.lld.events.Subject;

import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Shared identity, ACL, lock, and observer wiring for File and Folder.
 */
public abstract class DriveNode extends Subject implements FileSystemComponent {
    private final String id = UUID.randomUUID().toString();
    private final ReentrantLock lock = new ReentrantLock();
    private final PermissionsDecorator permissions = new PermissionsDecorator();
    private String name;
    private Folder parentFolder;

    protected DriveNode(String name, Folder parentFolder) {
        this.name = name;
        this.parentFolder = parentFolder;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public Folder getParentFolder() {
        return parentFolder;
    }

    @Override
    public void setParentFolder(Folder parentFolder) {
        this.parentFolder = parentFolder;
    }

    @Override
    public PermissionsDecorator getPermissions() {
        return permissions;
    }

    @Override
    public ReentrantLock getLock() {
        return lock;
    }

    public void subscribe(DriveObserver observer) {
        attach(observer);
    }

    public void publish(String message) {
        notifyObservers(message);
    }
}
