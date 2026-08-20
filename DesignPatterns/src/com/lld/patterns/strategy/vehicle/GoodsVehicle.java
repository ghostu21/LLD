package com.lld.patterns.strategy.vehicle;

public class GoodsVehicle extends Vehicle {
    public GoodsVehicle(DriveStrategy driveStrategy) {
        super(driveStrategy);
    }
}
