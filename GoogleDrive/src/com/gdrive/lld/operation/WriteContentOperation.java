package com.gdrive.lld.operation;

import com.gdrive.lld.fs.File;

/**
 * Writes content and can restore the previous blob on undo.
 */
public final class WriteContentOperation extends FileOperation {
    private final File file;
    private final String newContent;
    private String previous = "";

    public WriteContentOperation(File file, String newContent) {
        this.file = file;
        this.newContent = newContent;
    }

    @Override
    protected boolean validate() {
        return newContent != null;
    }

    @Override
    protected void performOperation() {
        previous = file.readContent();
        try {
            file.writeContent(newContent);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Interrupted while writing", e);
        }
    }

    @Override
    public void undo() {
        try {
            file.writeContent(previous);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
