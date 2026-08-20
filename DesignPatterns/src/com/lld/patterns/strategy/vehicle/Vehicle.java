package com.lld.patterns.strategy.vehicle;

/**
 * Context: holds a DriveStrategy and delegates. Subclasses describe *what* the
 * vehicle is; the strategy describes *how* it drives.
 */
public class Vehicle {
    private DriveStrategy driveStrategy;

    public Vehicle(DriveStrategy driveStrategy) {
        this.driveStrategy = driveStrategy;
    }

    public void setDriveStrategy(DriveStrategy driveStrategy) {
        this.driveStrategy = driveStrategy;
    }

    public void drive() {
        System.out.print("\n" + this.getClass().getSimpleName() + ": ");
        driveStrategy.drive();
    }
}
