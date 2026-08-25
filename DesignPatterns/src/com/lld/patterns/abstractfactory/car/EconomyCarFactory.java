package com.lld.patterns.abstractfactory.car;

public class EconomyCarFactory implements CarFactory {
    private final String brand;

    public EconomyCarFactory(String brand) {
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    @Override
    public CarInterior createInterior() {
        return new EconomyCarInterior();
    }

    @Override
    public CarExterior createExterior() {
        return new EconomyCarExterior();
    }
}
