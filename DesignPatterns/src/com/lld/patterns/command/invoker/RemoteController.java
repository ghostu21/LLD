package com.lld.patterns.command.invoker;

import com.lld.patterns.command.ICommand;

import java.util.Stack;

/**
 * Invoker: stores the command, runs execute() on button press, pops history for undo.
 */
public class RemoteController {
    private ICommand command;
    private final Stack<ICommand> commandHistory = new Stack<>();

    public void setCommand(ICommand command) {
        this.command = command;
    }

    public void pressButton() {
        command.execute();
        commandHistory.push(command);
    }

    public void undo() {
        if (!commandHistory.isEmpty()) {
            ICommand lastCommand = commandHistory.pop();
            lastCommand.undo();
        }
    }
}
