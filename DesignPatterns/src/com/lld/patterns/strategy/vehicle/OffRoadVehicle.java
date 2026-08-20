package com.lld.patterns.strategy.vehicle;

public class OffRoadVehicle extends Vehicle {
    public OffRoadVehicle(DriveStrategy driveStrategy) {
        super(driveStrategy);
    }
}
