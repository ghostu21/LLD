package com.lld.patterns.command.ac;

/**
 * Receiver: the AC device. The remote never calls these methods directly — commands do.
 */
public class AirConditioner {
    private boolean isOn;
    private int temperature;

    public void turnOn() {
        isOn = true;
        System.out.println("Air conditioner is on");
    }

    public void turnOff() {
        isOn = false;
        System.out.println("Air conditioner is off");
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOn(boolean on) {
        isOn = on;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        System.out.println("Air conditioner temperature set to " + temperature + "°C");
    }
}
