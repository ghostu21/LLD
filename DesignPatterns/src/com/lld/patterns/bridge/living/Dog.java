package com.lld.patterns.bridge.living;

import com.lld.patterns.bridge.breathing.BreathingProcess;

public class Dog extends LivingThings {
    public Dog(BreathingProcess breathingProcess) {
        super(breathingProcess);
    }

    @Override
    public void breathe() {
        System.out.print("Dog: ");
        breathingProcess.breathe();
    }
}
