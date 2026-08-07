package com.carrental.lld.vehicle;

/**
 * Thrown when a vehicle barcode does not exist in inventory.
 */
public class VehicleNotFoundException extends RuntimeException {
    /**
     * @param barcode missing vehicle barcode
     */
    public VehicleNotFoundException(String barcode) {
        super("Vehicle not found: " + barcode);
    }
}
