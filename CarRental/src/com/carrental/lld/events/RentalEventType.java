package com.carrental.lld.events;

/**
 * Rental domain events published to the async event bus.
 */
public enum RentalEventType {
    RESERVATION_CONFIRMED,
    RESERVATION_CANCELLED,
    PICKUP_REMINDER,
    DUE_REMINDER,
    OVERDUE,
    PAYMENT_COMPLETED,
    RETURNED
}
