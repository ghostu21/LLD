package com.gdrive.lld.demo;

import com.gdrive.lld.access.AccessLevel;
import com.gdrive.lld.fs.File;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;

/**
 * Shared users are observers and get content-update events.
 */
public final class ObserverScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Observer ---");
        Folder docs = FileSystemFactory.createFolder("Docs", fx.root, fx.owner);
        File notes = FileSystemFactory.createFile("notes.txt", docs, fx.owner);
        fx.drive.share(fx.owner, notes, fx.editor, AccessLevel.EDITOR);
        fx.drive.write(fx.owner, notes, "new draft");
    }
}
