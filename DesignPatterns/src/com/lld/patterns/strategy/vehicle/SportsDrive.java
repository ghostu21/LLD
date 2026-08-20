package com.lld.patterns.strategy.vehicle;

public class SportsDrive implements DriveStrategy {
    @Override
    public void drive() {
        System.out.println("Driving Capability: Sports");
    }
}
