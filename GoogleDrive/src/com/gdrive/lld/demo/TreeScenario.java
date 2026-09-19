package com.gdrive.lld.demo;

import com.gdrive.lld.fs.File;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;

/**
 * Composite tree: folder + file under root.
 */
public final class TreeScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Composite tree ---");
        Folder reports = FileSystemFactory.createFolder("Reports", fx.root, fx.owner);
        File sales = FileSystemFactory.createFile("SalesReport.pdf", reports, fx.owner);
        System.out.println("Created " + sales.getName() + " under " + reports.getName());
        fx.root.display("");
    }
}
