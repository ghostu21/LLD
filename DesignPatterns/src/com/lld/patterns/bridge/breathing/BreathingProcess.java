package com.lld.patterns.bridge.breathing;

/** Implementor: the "how" of breathing. Living things do not own this logic. */
public interface BreathingProcess {
    void breathe();
}
