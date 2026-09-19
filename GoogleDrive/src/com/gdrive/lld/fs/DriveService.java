package com.gdrive.lld.fs;

import com.gdrive.lld.access.AccessLevel;
import com.gdrive.lld.account.User;
import com.gdrive.lld.proxy.FileSystemProxy;
import com.gdrive.lld.quota.QuotaService;

/**
 * Facade used by demos: ACL via proxy, quota on write, persist via strategy.
 */
public final class DriveService {
    private final FileSystemManager manager;
    private final QuotaService quotaService;

    public DriveService(FileSystemManager manager, QuotaService quotaService) {
        this.manager = manager;
        this.quotaService = quotaService;
    }

    public FileSystemProxy as(User user, FileSystemComponent component) {
        return new FileSystemProxy(component, user);
    }

    public void write(User user, File file, String content) throws InterruptedException {
        as(user, file).require(AccessLevel.EDITOR);
        int oldSize = file.sizeBytes();
        quotaService.applyDelta(file.getOwner(), content.getBytes().length - oldSize);
        file.writeContent(content);
        manager.getStorageStrategy().saveFile(file);
    }

    public String read(User user, File file) {
        as(user, file).require(AccessLevel.VIEWER);
        return file.readContent();
    }

    public void comment(User user, File file, String text) {
        as(user, file).require(AccessLevel.COMMENTER);
        file.addComment(text);
    }

    public void share(User actor, FileSystemComponent node, User target, AccessLevel level) {
        as(actor, node).require(AccessLevel.OWNER);
        node.getPermissions().setPermission(target, level);
        if (node instanceof DriveNode) {
            ((DriveNode) node).subscribe(target);
            ((DriveNode) node).publish("Shared " + node.getName() + " with " + target.getUsername() + " as " + level);
        }
    }
}
