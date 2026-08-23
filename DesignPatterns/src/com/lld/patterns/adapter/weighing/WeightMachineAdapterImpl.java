package com.lld.patterns.adapter.weighing;

/**
 * Concrete adapter: IS-A {@link WeighingMachineAdapter}, HAS-A {@link ImperialWeighingMachine}.
 * Converts pounds → kg. The note uses 0.45 as a simplified factor (1 lb ≈ 0.453592 kg).
 */
public class WeightMachineAdapterImpl implements WeighingMachineAdapter {
    private final ImperialWeighingMachine imperialWeighingMachine;

    public WeightMachineAdapterImpl(ImperialWeighingMachine weightMachineInPounds) {
        this.imperialWeighingMachine = weightMachineInPounds;
    }

    @Override
    public double getWeightInKg() {
        double weightInPound = imperialWeighingMachine.getWeightInPounds();
        return weightInPound * 0.45;
    }
}
