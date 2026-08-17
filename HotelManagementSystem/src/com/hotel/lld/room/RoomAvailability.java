package com.hotel.lld.room;

import java.time.LocalDate;

/**
 * Availability flag for one calendar date on a room.
 * <p>
 * Why: booking is date-based, not only status-based — a room can be OCCUPIED
 * tonight and still bookable next month.
 */
public class RoomAvailability {
    private final LocalDate date;
    private boolean available;

    public RoomAvailability(LocalDate date, boolean available) {
        this.date = date;
        this.available = available;
    }

    public LocalDate getDate() {
        return date;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }
}
