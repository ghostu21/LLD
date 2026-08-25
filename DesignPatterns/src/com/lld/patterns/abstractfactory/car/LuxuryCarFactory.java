package com.lld.patterns.abstractfactory.car;

public class LuxuryCarFactory implements CarFactory {
    private final String brand;

    public LuxuryCarFactory(String brand) {
        this.brand = brand;
    }

    public String getBrand() {
        return brand;
    }

    @Override
    public CarInterior createInterior() {
        return new LuxuryCarInterior();
    }

    @Override
    public CarExterior createExterior() {
        return new LuxuryCarExterior();
    }
}
