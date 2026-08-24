package com.lld.patterns.bridge.living;

import com.lld.patterns.bridge.breathing.BreathingProcess;

/**
 * Abstraction: high-level "what" (a living thing). Holds the implementor (the bridge).
 */
public abstract class LivingThings {
    protected final BreathingProcess breathingProcess;

    public LivingThings(BreathingProcess breathingProcess) {
        this.breathingProcess = breathingProcess;
    }

    /** Delegates the "how" to {@link BreathingProcess}. */
    public abstract void breathe();
}
