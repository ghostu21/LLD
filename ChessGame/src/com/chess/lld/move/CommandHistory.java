package com.chess.lld.move;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * Undo / redo stacks (O(1) push/pop).
 * <p>
 * Why: {@code ArrayList<Move>} is O(N) to search and awkward for redo;
 * two deques give O(1) undo/redo. New moves clear the redo stack.
 */
public final class CommandHistory {
    private final Deque<MoveCommand> undoStack = new ArrayDeque<>();
    private final Deque<MoveCommand> redoStack = new ArrayDeque<>();

    public void push(MoveCommand move) {
        undoStack.push(move);
        redoStack.clear();
    }

    public MoveCommand popUndo() {
        return undoStack.isEmpty() ? null : undoStack.pop();
    }

    public void pushUndoWithoutClearingRedo(MoveCommand move) {
        undoStack.push(move);
    }

    public MoveCommand popRedo() {
        return redoStack.isEmpty() ? null : redoStack.pop();
    }

    public void pushRedo(MoveCommand move) {
        redoStack.push(move);
    }

    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    public List<MoveCommand> movesInOrder() {
        List<MoveCommand> list = new ArrayList<>(undoStack);
        java.util.Collections.reverse(list);
        return list;
    }
}
