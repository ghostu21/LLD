package com.gdrive.lld.demo;

import com.gdrive.lld.fs.File;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;

/**
 * Write two versions then revert.
 */
public final class VersionScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Version control ---");
        Folder docs = FileSystemFactory.createFolder("Docs", fx.root, fx.owner);
        File notes = FileSystemFactory.createFile("notes.txt", docs, fx.owner);
        fx.drive.write(fx.owner, notes, "v1");
        fx.drive.write(fx.owner, notes, "v2-final");
        System.out.println("Versions: " + notes.getVersions().size());
        notes.revertTo(1);
        System.out.println("After revert to v1: " + notes.readContent());
    }
}
