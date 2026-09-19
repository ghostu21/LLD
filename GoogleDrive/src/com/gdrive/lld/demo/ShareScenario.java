package com.gdrive.lld.demo;

import com.gdrive.lld.access.AccessDeniedException;
import com.gdrive.lld.access.AccessLevel;
import com.gdrive.lld.fs.File;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;
import com.gdrive.lld.proxy.FileSystemProxy;

/**
 * Folder-level group share, file-level NONE override, inheritance walk.
 */
public final class ShareScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Share / inheritance / NONE override ---");
        Folder reports = FileSystemFactory.createFolder("Reports", fx.root, fx.owner);
        File sales = FileSystemFactory.createFile("SalesReport.pdf", reports, fx.owner);

        reports.getPermissions().setPermission(fx.editorsGroup, AccessLevel.EDITOR);
        reports.getPermissions().setPermission(fx.viewersGroup, AccessLevel.VIEWER);
        sales.getPermissions().setPermission(fx.viewer, AccessLevel.NONE);

        FileSystemProxy editorView = new FileSystemProxy(reports, fx.editor);
        System.out.println("Editor on Reports: " + editorView.effectiveAccess());

        FileSystemProxy viewerFolder = new FileSystemProxy(reports, fx.viewer);
        System.out.println("Viewer on Reports (inherited group): " + viewerFolder.effectiveAccess());

        FileSystemProxy viewerFile = new FileSystemProxy(sales, fx.viewer);
        System.out.println("Viewer on SalesReport (explicit NONE): " + viewerFile.effectiveAccess());
        try {
            viewerFile.display("");
        } catch (AccessDeniedException e) {
            System.out.println("Expected deny: " + e.getMessage());
        }
    }
}
