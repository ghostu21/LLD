package com.gdrive.lld.fs;

import com.gdrive.lld.access.AccessLevel;
import com.gdrive.lld.account.User;

/**
 * Factory for File / Folder so creation (owner ACL, parent link) stays in one place.
 */
public final class FileSystemFactory {
    private FileSystemFactory() {
    }

    public static Folder createFolder(String name, Folder parent, User owner) {
        Folder folder = new Folder(name, parent);
        folder.getPermissions().setPermission(owner, AccessLevel.OWNER);
        if (parent != null) {
            parent.add(folder);
        }
        return folder;
    }

    public static File createFile(String name, Folder parent, User owner) {
        File file = new File(name, parent, owner);
        file.getPermissions().setPermission(owner, AccessLevel.OWNER);
        parent.add(file);
        return file;
    }
}
