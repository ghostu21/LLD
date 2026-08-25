package com.lld.patterns.abstractfactory.car;

public class LuxuryCarInterior implements CarInterior {
    @Override
    public void addInteriorComponents() {
        System.out.println("Adding luxurious interior components for Luxury Car.");
    }
}
