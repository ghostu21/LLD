package com.hotel.lld.booking;

/** Thrown when per-room lock cannot be acquired in time. */
public class BookingTimeoutException extends RuntimeException {
    public BookingTimeoutException(String roomNumber) {
        super("Timed out acquiring lock for room: " + roomNumber);
    }
}
