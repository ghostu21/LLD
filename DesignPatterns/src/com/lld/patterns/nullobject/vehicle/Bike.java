package com.lld.patterns.nullobject.vehicle;

public class Bike extends Vehicle {
    private final String model;
    private final String color;
    private final int seatingCapacity;
    private final int fuelTankCapacity;
    private final boolean isAvailableForTestDrive;

    public Bike(String model, String color, int fuelTankCapacity) {
        this.model = model;
        this.color = color;
        this.fuelTankCapacity = fuelTankCapacity;
        this.isAvailableForTestDrive = false;
        this.seatingCapacity = 2;
    }

    @Override
    public void start() {
        System.out.println("Bike is started and moving");
    }

    @Override
    public void stop() {
        System.out.println("Bike is stopped");
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
