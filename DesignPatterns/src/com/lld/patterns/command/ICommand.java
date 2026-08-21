package com.lld.patterns.command;

/**
 * Encapsulates a request. Invoker calls execute(); undo() restores prior receiver state.
 */
public interface ICommand {
    void execute();

    void undo();
}
