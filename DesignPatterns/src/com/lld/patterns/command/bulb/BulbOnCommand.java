package com.lld.patterns.command.bulb;

import com.lld.patterns.command.ICommand;

public class BulbOnCommand implements ICommand {
    private final Bulb bulb;
    private boolean previousState;

    public BulbOnCommand(Bulb bulb) {
        this.bulb = bulb;
    }

    @Override
    public void execute() {
        previousState = bulb.isOn();
        bulb.turnOn();
    }

    @Override
    public void undo() {
        System.out.print("Undo: Bulb On command. ");
        if (!previousState) {
            bulb.turnOff();
        } else {
            System.out.println();
        }
    }
}
