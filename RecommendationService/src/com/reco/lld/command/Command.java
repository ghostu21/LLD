package com.reco.lld.command;

/**
 * Command pattern for mutating feedback (undo-ready in a real system).
 */
public interface Command {
    void execute();
}
