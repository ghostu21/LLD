package com.gdrive.lld.demo;

import com.gdrive.lld.fs.File;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;
import com.gdrive.lld.storage.CloudStorageStrategy;

/**
 * Swap LocalStorageStrategy for CloudStorageStrategy.
 */
public final class StorageScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Storage strategy ---");
        Folder docs = FileSystemFactory.createFolder("Docs", fx.root, fx.owner);
        File notes = FileSystemFactory.createFile("notes.txt", docs, fx.owner);
        fx.drive.write(fx.owner, notes, "local-bytes");
        fx.manager.setStorageStrategy(new CloudStorageStrategy());
        fx.drive.write(fx.owner, notes, "cloud-bytes");
    }
}
