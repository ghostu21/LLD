package com.lld.patterns.bridge.living;

import com.lld.patterns.bridge.breathing.BreathingProcess;

public class Tree extends LivingThings {
    public Tree(BreathingProcess breathingProcess) {
        super(breathingProcess);
    }

    @Override
    public void breathe() {
        System.out.print("Tree: ");
        breathingProcess.breathe();
    }
}
