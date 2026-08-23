package com.lld.patterns.composite.filesystem;

/** Leaf: a file has no children. */
public class File implements FileSystemComponent {
    private final String fileName;

    public File(String name) {
        this.fileName = name;
    }

    @Override
    public void printContents() {
        System.out.println("File name: " + fileName);
    }
}
