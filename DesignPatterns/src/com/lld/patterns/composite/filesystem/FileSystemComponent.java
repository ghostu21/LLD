package com.lld.patterns.composite.filesystem;

/** Component: same operation for a file (leaf) and a directory (composite). */
public interface FileSystemComponent {
    void printContents();
}
