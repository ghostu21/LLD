package com.gdrive.lld.operation;

import com.gdrive.lld.fs.FileSystemComponent;
import com.gdrive.lld.fs.FileSystemManager;
import com.gdrive.lld.fs.Folder;

/**
 * Move under ordered locks (see {@link FileSystemManager#move}).
 */
public final class MoveOperation extends FileOperation {
    private final FileSystemComponent source;
    private final Folder destination;
    private Folder previousParent;

    public MoveOperation(FileSystemComponent source, Folder destination) {
        this.source = source;
        this.destination = destination;
    }

    @Override
    protected boolean validate() {
        return source.getParentFolder() != null && destination.getChild(source.getName()) == null;
    }

    @Override
    protected void performOperation() {
        previousParent = source.getParentFolder();
        try {
            FileSystemManager.getInstance().move(source, destination);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted during move", e);
        }
    }

    @Override
    public void undo() {
        try {
            FileSystemManager.getInstance().move(source, previousParent);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
