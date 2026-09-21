package com.vending.lld.command;

/**
 * Command for a money movement that can be logged and rolled back.
 */
public interface PaymentCommand {
    void execute();

    void undo();

    String description();
}
