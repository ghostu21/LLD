package com.gdrive.lld.demo;

import com.gdrive.lld.access.AccessDeniedException;
import com.gdrive.lld.access.AccessLevel;
import com.gdrive.lld.fs.File;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;
import com.gdrive.lld.proxy.FileSystemProxy;

/**
 * Proxy blocks viewer rename; editor is allowed.
 */
public final class ProxyScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Proxy ACL ---");
        Folder reports = FileSystemFactory.createFolder("Reports", fx.root, fx.owner);
        File sales = FileSystemFactory.createFile("SalesReport.pdf", reports, fx.owner);
        reports.getPermissions().setPermission(fx.editorsGroup, AccessLevel.EDITOR);
        reports.getPermissions().setPermission(fx.viewersGroup, AccessLevel.VIEWER);

        FileSystemProxy editorProxy = new FileSystemProxy(sales, fx.editor);
        editorProxy.setName("Q1-Sales.pdf");
        System.out.println("Editor renamed to " + sales.getName());

        FileSystemProxy viewerProxy = new FileSystemProxy(sales, fx.viewer);
        try {
            viewerProxy.setName("hacked.pdf");
        } catch (AccessDeniedException e) {
            System.out.println("Expected deny: " + e.getMessage());
        }
    }
}
