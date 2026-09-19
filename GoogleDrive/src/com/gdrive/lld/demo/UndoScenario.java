package com.gdrive.lld.demo;

import com.gdrive.lld.fs.File;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;
import com.gdrive.lld.operation.CommandHistory;
import com.gdrive.lld.operation.CreateFileOperation;
import com.gdrive.lld.operation.WriteContentOperation;

/**
 * Template-method commands with undo.
 */
public final class UndoScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Command undo ---");
        Folder docs = FileSystemFactory.createFolder("Docs", fx.root, fx.owner);
        CommandHistory history = new CommandHistory();
        CreateFileOperation create = new CreateFileOperation(docs, "notes.txt", fx.owner);
        history.execute(create);
        File notes = create.getCreated();
        history.execute(new WriteContentOperation(notes, "hello"));
        System.out.println("After write: " + notes.readContent());
        history.undo();
        System.out.println("After undo write: '" + notes.readContent() + "'");
        history.undo();
        System.out.println("After undo create, child exists? " + (docs.getChild("notes.txt") != null));
    }
}
