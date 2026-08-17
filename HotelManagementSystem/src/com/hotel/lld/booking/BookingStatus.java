package com.hotel.lld.booking;

/** Lifecycle of a room reservation. */
public enum BookingStatus {
    REQUESTED, PENDING, CONFIRMED, CHECKED_IN, CHECKED_OUT, CANCELLED, ABANDONED
}
