package com.lld.patterns.bridge.living;

import com.lld.patterns.bridge.breathing.BreathingProcess;

public class Fish extends LivingThings {
    public Fish(BreathingProcess breathingProcess) {
        super(breathingProcess);
    }

    @Override
    public void breathe() {
        System.out.print("Fish: ");
        breathingProcess.breathe();
    }
}
