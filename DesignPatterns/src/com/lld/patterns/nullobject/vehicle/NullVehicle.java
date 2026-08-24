package com.lld.patterns.nullobject.vehicle;

/**
 * Null object: same type as a real vehicle; start/stop are no-ops (logged).
 * Factory returns this instead of {@code null}.
 */
public class NullVehicle extends Vehicle {
    private final String model;
    private final String color;
    private final int seatingCapacity;
    private final int fuelTankCapacity;
    private final boolean isAvailableForTestDrive;

    public NullVehicle() {
        this.model = "Default";
        this.color = "Default";
        this.seatingCapacity = 0;
        this.fuelTankCapacity = 0;
        this.isAvailableForTestDrive = false;
    }

    @Override
    public void start() {
        System.out.print("\n[-] Null Vehicle: start() - do nothing");
    }

    @Override
    public void stop() {
        System.out.println("\n[-] Null Vehicle: stop() - do nothing");
    }

    @Override
    public String getModel() {
        return model;
    }

    @Override
    public String getColor() {
        return color;
    }

    @Override
    public int getSeatingCapacity() {
        return seatingCapacity;
    }

    @Override
    public int getFuelTankCapacity() {
        return fuelTankCapacity;
    }

    @Override
    public boolean isAvailableForTestDrive() {
        return isAvailableForTestDrive;
    }
}
