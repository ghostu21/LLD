package com.lld.patterns.bridge.breathing;

public class GillBreathing implements BreathingProcess {
    @Override
    public void breathe() {
        System.out.println("Breathing through gills.");
    }
}
