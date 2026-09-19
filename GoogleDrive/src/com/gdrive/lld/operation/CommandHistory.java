package com.gdrive.lld.operation;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Undo stack of executed {@link FileOperation}s.
 */
public final class CommandHistory {
    private final Deque<FileOperation> undoStack = new ArrayDeque<>();

    public void execute(FileOperation operation) {
        operation.execute();
        undoStack.push(operation);
    }

    public boolean undo() {
        if (undoStack.isEmpty()) {
            return false;
        }
        undoStack.pop().undo();
        return true;
    }
}
