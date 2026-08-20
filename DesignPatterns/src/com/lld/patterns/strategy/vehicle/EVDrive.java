package com.lld.patterns.strategy.vehicle;

public class EVDrive implements DriveStrategy {
    @Override
    public void drive() {
        System.out.println("Driving Capability: Electric");
    }
}
