package com.lld.patterns.bridge.living;

import com.lld.patterns.bridge.breathing.BreathingProcess;

/** Same lungs as Dog, different living thing — no WhaleWithLungs subclass. */
public class Whale extends LivingThings {
    public Whale(BreathingProcess breathingProcess) {
        super(breathingProcess);
    }

    @Override
    public void breathe() {
        System.out.print("Whale: ");
        breathingProcess.breathe();
    }
}
