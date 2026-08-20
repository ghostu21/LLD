package com.lld.patterns.strategy.vehicle;

public class HybridVehicle extends Vehicle {
    public HybridVehicle(DriveStrategy driveStrategy) {
        super(driveStrategy);
    }
}
