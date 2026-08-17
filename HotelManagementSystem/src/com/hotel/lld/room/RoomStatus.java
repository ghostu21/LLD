package com.hotel.lld.room;

/**
 * Operational status of a physical room.
 * <p>
 * Interview note: date-based availability (calendar) decides future bookings;
 * this enum tracks current operational state (occupied / being serviced / etc.).
 */
public enum RoomStatus {
    AVAILABLE, RESERVED, OCCUPIED, NOT_AVAILABLE, BEING_SERVICED, OTHER
}
