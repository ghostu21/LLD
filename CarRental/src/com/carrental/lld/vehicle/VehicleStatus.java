package com.carrental.lld.vehicle;

/**
 * Lifecycle status of a vehicle in the fleet.
 * <p>
 * Why: inventory search and reservation transitions depend on whether a unit
 * is available, reserved, rented, or in maintenance.
 */
public enum VehicleStatus {
    AVAILABLE,
    RESERVED,
    RENTED,
    MAINTENANCE
}
