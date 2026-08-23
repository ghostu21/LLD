package com.lld.patterns.composite.filesystem;

import java.util.ArrayList;
import java.util.List;

/**
 * Composite: a directory holds children that are files or other directories.
 * {@code printContents()} just forwards — no {@code instanceof}.
 */
public class Directory implements FileSystemComponent {
    private final String directoryName;
    private final List<FileSystemComponent> children = new ArrayList<>();

    public Directory(String name) {
        this.directoryName = name;
    }

    public void add(FileSystemComponent fileSystemComponent) {
        children.add(fileSystemComponent);
    }

    public void remove(FileSystemComponent fileSystemComponent) {
        children.remove(fileSystemComponent);
    }

    @Override
    public void printContents() {
        System.out.println("Directory Name: " + directoryName);
        for (FileSystemComponent child : children) {
            child.printContents();
        }
    }
}
