package com.hotel.lld.booking;

/** Thrown when a room cannot be booked for the requested dates. */
public class RoomNotAvailableException extends RuntimeException {
    public RoomNotAvailableException(String roomNumber, String reason) {
        super("Room " + roomNumber + " not available: " + reason);
    }
}
