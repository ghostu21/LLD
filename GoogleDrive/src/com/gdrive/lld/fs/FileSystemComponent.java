package com.gdrive.lld.fs;

import com.gdrive.lld.access.PermissionsDecorator;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Composite node: files and folders share name, parent, ACL, and a lock.
 */
public interface FileSystemComponent {
    String getId();

    String getName();

    void setName(String name);

    Folder getParentFolder();

    void setParentFolder(Folder parent);

    PermissionsDecorator getPermissions();

    ReentrantLock getLock();

    void add(FileSystemComponent component);

    void remove(FileSystemComponent component);

    FileSystemComponent getChild(String name);

    void display(String indent);
}
