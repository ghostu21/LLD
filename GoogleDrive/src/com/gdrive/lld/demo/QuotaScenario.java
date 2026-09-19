package com.gdrive.lld.demo;

import com.gdrive.lld.fs.File;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;
import com.gdrive.lld.quota.QuotaExceededException;

/**
 * Per-user byte cap rejects an oversized write.
 */
public final class QuotaScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Storage quota ---");
        fx.quota.setLimit(fx.owner, 20);
        Folder docs = FileSystemFactory.createFolder("Docs", fx.root, fx.owner);
        File notes = FileSystemFactory.createFile("notes.txt", docs, fx.owner);
        fx.drive.write(fx.owner, notes, "ok");
        System.out.println("Used after small write: " + fx.quota.used(fx.owner));
        try {
            fx.drive.write(fx.owner, notes, "this-content-is-way-too-large");
            System.out.println("ERROR: quota should have failed");
        } catch (QuotaExceededException e) {
            System.out.println("Expected failure: " + e.getMessage());
        }
    }
}
