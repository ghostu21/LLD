package com.lld.patterns.bridge.breathing;

public class LungBreathing implements BreathingProcess {
    @Override
    public void breathe() {
        System.out.println("Breathing through lungs.");
    }
}
