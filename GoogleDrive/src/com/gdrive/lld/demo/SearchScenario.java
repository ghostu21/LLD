package com.gdrive.lld.demo;

import com.gdrive.lld.access.AccessLevel;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;
import com.gdrive.lld.search.FileSystemSearch;

/**
 * DFS search by name, only nodes the viewer can see.
 */
public final class SearchScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Search (DFS) ---");
        Folder docs = FileSystemFactory.createFolder("Docs", fx.root, fx.owner);
        FileSystemFactory.createFile("roadmap.txt", docs, fx.owner);
        FileSystemFactory.createFile("secret.txt", docs, fx.owner);
        docs.getPermissions().setPermission(fx.viewersGroup, AccessLevel.VIEWER);
        docs.getChild("secret.txt").getPermissions().setPermission(fx.viewer, AccessLevel.NONE);

        FileSystemSearch search = new FileSystemSearch();
        System.out.println("Owner hits for 'txt': " + search.searchAll(fx.root, "txt", fx.owner).size());
        System.out.println("Viewer hits for 'txt': " + search.searchAll(fx.root, "txt", fx.viewer).size()
                + " (secret hidden)");
    }
}
