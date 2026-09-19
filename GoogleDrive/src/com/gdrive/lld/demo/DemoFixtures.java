package com.gdrive.lld.demo;

import com.gdrive.lld.access.AccessLevel;
import com.gdrive.lld.account.Group;
import com.gdrive.lld.account.Role;
import com.gdrive.lld.account.User;
import com.gdrive.lld.account.UserService;
import com.gdrive.lld.fs.DriveService;
import com.gdrive.lld.fs.FileSystemManager;
import com.gdrive.lld.fs.Folder;
import com.gdrive.lld.quota.QuotaService;

/**
 * Fresh singleton tree + users for each scenario.
 */
public final class DemoFixtures {
    public final FileSystemManager manager;
    public final UserService users;
    public final QuotaService quota;
    public final DriveService drive;
    public final User owner;
    public final User editor;
    public final User viewer;
    public final User admin;
    public final Group editorsGroup;
    public final Group viewersGroup;
    public final Folder root;

    public DemoFixtures() {
        manager = FileSystemManager.getInstance();
        manager.reset();
        users = new UserService();
        quota = new QuotaService();
        drive = new DriveService(manager, quota);

        owner = users.register("owner", "ownerpass", Role.USER);
        editor = users.register("editor", "editorpass", Role.USER);
        viewer = users.register("viewer", "viewerpass", Role.USER);
        admin = users.register("admin", "adminpass", Role.ADMIN);

        editorsGroup = new Group("Editors");
        viewersGroup = new Group("Viewers");
        editorsGroup.addUser(editor);
        viewersGroup.addUser(viewer);

        root = manager.getRoot();
        root.getPermissions().setPermission(owner, AccessLevel.OWNER);
        quota.setLimit(owner, QuotaService.DEFAULT_LIMIT_BYTES);
    }
}
