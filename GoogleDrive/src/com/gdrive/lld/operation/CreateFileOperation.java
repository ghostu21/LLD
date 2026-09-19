package com.gdrive.lld.operation;

import com.gdrive.lld.account.User;
import com.gdrive.lld.fs.File;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;

/**
 * Creates a file in a folder (Template Method + Command undo = delete).
 */
public final class CreateFileOperation extends FileOperation {
    private final Folder folder;
    private final String fileName;
    private final User owner;
    private File created;

    public CreateFileOperation(Folder folder, String fileName, User owner) {
        this.folder = folder;
        this.fileName = fileName;
        this.owner = owner;
    }

    public File getCreated() {
        return created;
    }

    @Override
    protected boolean validate() {
        return folder.getChild(fileName) == null;
    }

    @Override
    protected void performOperation() {
        created = FileSystemFactory.createFile(fileName, folder, owner);
    }

    @Override
    public void undo() {
        if (created != null) {
            folder.remove(created);
        }
    }
}
