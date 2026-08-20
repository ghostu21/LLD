package com.lld.patterns.strategy.vehicle;

/**
 * Strategy contract for how a vehicle drives.
 * New modes (off-road, eco, autopilot) implement this — they do not edit Vehicle.
 */
public interface DriveStrategy {
    void drive();
}
