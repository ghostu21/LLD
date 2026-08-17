package com.hotel.lld.events;

/** Domain event types for hotel notifications. */
public enum HotelEventType {
    BOOKING_CONFIRMED,
    BOOKING_CANCELLED,
    CHECK_IN_REMINDER,
    CHECK_OUT_REMINDER,
    CHECKED_IN,
    CHECKED_OUT,
    PAYMENT_COMPLETED
}
