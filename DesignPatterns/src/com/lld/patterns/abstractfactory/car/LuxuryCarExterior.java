package com.lld.patterns.abstractfactory.car;

public class LuxuryCarExterior implements CarExterior {
    @Override
    public void addExteriorComponents() {
        System.out.println("Adding luxurious exterior components for Luxury Car.");
    }
}
