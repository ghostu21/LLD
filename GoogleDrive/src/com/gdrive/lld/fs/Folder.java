package com.gdrive.lld.fs;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Composite node: children in a ConcurrentHashMap, structural mutations synchronized.
 */
public final class Folder extends DriveNode {
    private final ConcurrentHashMap<String, FileSystemComponent> children = new ConcurrentHashMap<>();

    public Folder(String name, Folder parentFolder) {
        super(name, parentFolder);
    }

    @Override
    public void add(FileSystemComponent component) {
        synchronized (this) {
            children.put(component.getName(), component);
            component.setParentFolder(this);
        }
    }

    @Override
    public void remove(FileSystemComponent component) {
        synchronized (this) {
            children.remove(component.getName());
        }
    }

    @Override
    public FileSystemComponent getChild(String name) {
        return children.get(name);
    }

    public Map<String, FileSystemComponent> getChildren() {
        return children;
    }

    public Collection<FileSystemComponent> snapshotChildren() {
        return children.values();
    }

    @Override
    public void display(String indent) {
        System.out.println(indent + "Folder: " + getName());
        for (FileSystemComponent child : children.values()) {
            child.display(indent + "    ");
        }
    }
}
