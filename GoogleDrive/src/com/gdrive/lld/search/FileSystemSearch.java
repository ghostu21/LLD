package com.gdrive.lld.search;

import com.gdrive.lld.access.AccessLevel;
import com.gdrive.lld.account.User;
import com.gdrive.lld.fs.FileSystemComponent;
import com.gdrive.lld.fs.Folder;
import com.gdrive.lld.proxy.FileSystemProxy;

import java.util.ArrayList;
import java.util.List;

/**
 * Depth-first name search over the composite tree, skipping nodes the user cannot view.
 */
public final class FileSystemSearch {
    public FileSystemComponent search(FileSystemComponent component, String name, User user) {
        List<FileSystemComponent> hits = searchAll(component, name, user);
        return hits.isEmpty() ? null : hits.get(0);
    }

    public List<FileSystemComponent> searchAll(FileSystemComponent component, String name, User user) {
        List<FileSystemComponent> hits = new ArrayList<>();
        dfs(component, name.toLowerCase(), user, hits);
        return hits;
    }

    private void dfs(FileSystemComponent component, String needle, User user, List<FileSystemComponent> hits) {
        synchronized (component) {
            FileSystemProxy proxy = new FileSystemProxy(component, user);
            boolean visible = true;
            try {
                proxy.require(AccessLevel.VIEWER);
            } catch (RuntimeException e) {
                visible = false;
            }
            if (visible && component.getName().toLowerCase().contains(needle)) {
                hits.add(component);
            }
            if (component instanceof Folder) {
                for (FileSystemComponent child : ((Folder) component).snapshotChildren()) {
                    dfs(child, needle, user, hits);
                }
            }
        }
    }
}
