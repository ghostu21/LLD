package com.carrental.lld.reservation;

/**
 * Thrown when a vehicle is not available for the requested window.
 */
public class VehicleNotAvailableException extends RuntimeException {
    /**
     * @param barcode vehicle barcode
     * @param reason  detail message
     */
    public VehicleNotAvailableException(String barcode, String reason) {
        super("Vehicle " + barcode + " not available: " + reason);
    }
}
