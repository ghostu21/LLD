package com.lld.patterns.adapter.weighing;

/**
 * Adaptee: third-party weighing machine (US model) — returns pounds.
 * We do not change this class.
 */
public class ImperialWeighingMachineImpl implements ImperialWeighingMachine {
    private final double weightInPounds;

    public ImperialWeighingMachineImpl(double weighingScaleReading) {
        this.weightInPounds = weighingScaleReading;
    }

    @Override
    public double getWeightInPounds() {
        return weightInPounds;
    }
}
