package com.lld.patterns.abstractfactory.car;

/**
 * Abstract factory: factory methods for a related family (interior + exterior).
 * {@link #produceCompleteVehicle()} is a template that uses those methods.
 */
public interface CarFactory {
    CarInterior createInterior();

    CarExterior createExterior();

    default void produceCompleteVehicle() {
        System.out.println("Starting complete vehicle production...");
        CarInterior interior = createInterior();
        CarExterior exterior = createExterior();
        interior.addInteriorComponents();
        exterior.addExteriorComponents();
        System.out.println("Vehicle production completed!");
    }
}
