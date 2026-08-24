package com.lld.patterns.bridge.breathing;

public class Photosynthesis implements BreathingProcess {
    @Override
    public void breathe() {
        System.out.println("Breathing through process of photosynthesis. Releases Oxygen through leaves.");
    }
}
