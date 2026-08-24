package com.lld.patterns.nullobject.vehicle;

/**
 * Abstract vehicle: real types and {@link NullVehicle} share this contract.
 * Client code calls these methods without a {@code null} check.
 */
public abstract class Vehicle {
    public abstract void start();

    public abstract void stop();

    public abstract String getModel();

    public abstract String getColor();

    public abstract int getSeatingCapacity();

    public abstract int getFuelTankCapacity();

    public abstract boolean isAvailableForTestDrive();
}
