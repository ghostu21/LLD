package com.lld.patterns.bridge.breathing;

/** Extra implementor from the note: amphibians (e.g. Frog) without a new animal×breath class. */
public class SkinBreathing implements BreathingProcess {
    @Override
    public void breathe() {
        System.out.println("Breathing through skin.");
    }
}
