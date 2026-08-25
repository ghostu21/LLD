package com.lld.patterns.abstractfactory.car;

/**
 * Simple-factory-of-factories: maps client input to a concrete {@link CarFactory}.
 * PREMIUM and LUXURY share the luxury family (same as the LLD note).
 */
public class CarFactoryProvider {
    public CarFactory getFactory(CarType type, String brand) {
        switch (type) {
            case ECONOMY:
                return new EconomyCarFactory(brand);
            case PREMIUM:
            case LUXURY:
                return new LuxuryCarFactory(brand);
            default:
                throw new IllegalArgumentException("Unknown car type: " + type);
        }
    }
}
