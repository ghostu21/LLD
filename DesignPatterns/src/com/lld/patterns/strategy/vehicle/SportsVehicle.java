package com.lld.patterns.strategy.vehicle;

public class SportsVehicle extends Vehicle {
    public SportsVehicle(DriveStrategy driveStrategy) {
        super(driveStrategy);
    }
}
