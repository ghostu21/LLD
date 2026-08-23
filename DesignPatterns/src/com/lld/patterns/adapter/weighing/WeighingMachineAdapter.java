package com.lld.patterns.adapter.weighing;

/** Target: the API the metric client already expects. */
public interface WeighingMachineAdapter {
    double getWeightInKg();
}
