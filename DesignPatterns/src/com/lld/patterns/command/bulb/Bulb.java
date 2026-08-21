package com.lld.patterns.command.bulb;

/**
 * Receiver: another device. The same RemoteController can invoke bulb commands
 * without knowing this class exists.
 */
public class Bulb {
    private boolean isOn;

    public void turnOn() {
        isOn = true;
        System.out.println("Bulb is on");
    }

    public void turnOff() {
        isOn = false;
        System.out.println("Bulb is off");
    }

    public boolean isOn() {
        return isOn;
    }
}
