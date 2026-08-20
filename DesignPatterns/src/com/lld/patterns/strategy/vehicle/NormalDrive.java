package com.lld.patterns.strategy.vehicle;

public class NormalDrive implements DriveStrategy {
    @Override
    public void drive() {
        System.out.println("Driving Capability: Normal");
    }
}
