package com.gdrive.lld.demo;

import com.gdrive.lld.fs.File;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;
import com.gdrive.lld.operation.MoveOperation;

/**
 * Ordered locks on move (name/id sort).
 */
public final class MoveScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Move (lock ordering) ---");
        Folder docs = FileSystemFactory.createFolder("Docs", fx.root, fx.owner);
        Folder archive = FileSystemFactory.createFolder("Archive", fx.root, fx.owner);
        File notes = FileSystemFactory.createFile("notes.txt", docs, fx.owner);
        new MoveOperation(notes, archive).execute();
        System.out.println("notes parent now: " + notes.getParentFolder().getName());
        System.out.println("Docs still has notes? " + (docs.getChild("notes.txt") != null));
    }
}
