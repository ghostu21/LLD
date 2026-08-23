package com.lld.patterns.adapter.demo;

import com.lld.patterns.adapter.weighing.ImperialWeighingMachineImpl;
import com.lld.patterns.adapter.weighing.WeighingMachineAdapter;
import com.lld.patterns.adapter.weighing.WeightMachineAdapterImpl;

/**
 * Client from the LLD note ({@code MetricWeighingMachine}): only calls {@code getWeightInKg()}.
 */
public class AdapterPatternDemo {
    public static void main(String[] args) {
        System.out.println("======= Adapter Design Pattern ======");

        double weighingScaleReading = 25.0;
        ImperialWeighingMachineImpl imperialWeighingMachine =
                new ImperialWeighingMachineImpl(weighingScaleReading);
        System.out.println("Weight in pounds (adaptee): " + imperialWeighingMachine.getWeightInPounds());

        WeighingMachineAdapter weightMachineAdapter =
                new WeightMachineAdapterImpl(imperialWeighingMachine);
        System.out.println("Weight in KG: " + weightMachineAdapter.getWeightInKg());
    }
}
