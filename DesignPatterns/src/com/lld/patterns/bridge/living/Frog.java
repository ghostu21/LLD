package com.lld.patterns.bridge.living;

import com.lld.patterns.bridge.breathing.BreathingProcess;

/** Note: add Frog without touching breathing classes; plug in SkinBreathing. */
public class Frog extends LivingThings {
    public Frog(BreathingProcess breathingProcess) {
        super(breathingProcess);
    }

    @Override
    public void breathe() {
        System.out.print("Frog: ");
        breathingProcess.breathe();
    }
}
