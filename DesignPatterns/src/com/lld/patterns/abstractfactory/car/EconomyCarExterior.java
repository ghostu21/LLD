package com.lld.patterns.abstractfactory.car;

public class EconomyCarExterior implements CarExterior {
    @Override
    public void addExteriorComponents() {
        System.out.println("Adding basic exterior components for Economy Car.");
    }
}
