package com.carrental.lld.reservation;

/**
 * Thrown when a reservation lock cannot be acquired within the timeout.
 */
public class ReservationTimeoutException extends RuntimeException {
    /**
     * @param barcode vehicle barcode
     */
    public ReservationTimeoutException(String barcode) {
        super("Timed out acquiring reservation lock for vehicle: " + barcode);
    }
}
