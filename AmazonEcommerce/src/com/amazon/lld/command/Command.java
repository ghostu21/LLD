package com.amazon.lld.command;

/**
 * Command pattern entry point for cart and checkout actions.
 * <p>
 * Why: encapsulates requests as objects for undo/redo or queueing (demo: execute only).
 */
public interface Command {
    /**
     * Executes the command side effect.
     */
    void execute();
}
