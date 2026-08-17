package com.hotel.lld.room;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hotel room with a date-based availability calendar and optimistic version.
 * <p>
 * Why: per-room {@code availabilityCalendar} prevents overbooking across future
 * nights; {@code version} supports optimistic concurrency (retry on mismatch).
 */
public class Room {
    private final String roomNumber;
    private final String hotelId;
    private final RoomStyle style;
    private final double bookingPrice;
    private final boolean smoking;
    private RoomStatus status;
    private final Map<LocalDate, Boolean> availabilityCalendar = new ConcurrentHashMap<>();
    /** Optimistic lock version — bump on each successful calendar mutation. */
    private final AtomicLong version = new AtomicLong(0);

    public Room(String roomNumber, String hotelId, RoomStyle style,
                double bookingPrice, boolean smoking) {
        this.roomNumber = roomNumber;
        this.hotelId = hotelId;
        this.style = style;
        this.bookingPrice = bookingPrice;
        this.smoking = smoking;
        this.status = RoomStatus.AVAILABLE;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getHotelId() {
        return hotelId;
    }

    public RoomStyle getStyle() {
        return style;
    }

    public double getBookingPrice() {
        return bookingPrice;
    }

    public boolean isSmoking() {
        return smoking;
    }

    public RoomStatus getStatus() {
        return status;
    }

    public void setStatus(RoomStatus status) {
        this.status = status;
    }

    public Map<LocalDate, Boolean> getAvailabilityCalendar() {
        return availabilityCalendar;
    }

    public long getVersion() {
        return version.get();
    }

    public long incrementVersion() {
        return version.incrementAndGet();
    }

    /**
     * Seeds availability for a closed date range (inclusive start, exclusive end).
     */
    public void seedAvailability(LocalDate from, LocalDate toExclusive, boolean available) {
        LocalDate d = from;
        while (d.isBefore(toExclusive)) {
            availabilityCalendar.putIfAbsent(d, available);
            d = d.plusDays(1);
        }
    }

    /**
     * Date-range availability check used by search and booking.
     *
     * @param start  check-in date
     * @param nights number of nights
     * @return true if every night in the stay is free
     */
    public boolean isAvailable(LocalDate start, int nights) {
        if (status == RoomStatus.NOT_AVAILABLE || status == RoomStatus.BEING_SERVICED) {
            return false;
        }
        for (int i = 0; i < nights; i++) {
            LocalDate day = start.plusDays(i);
            Boolean free = availabilityCalendar.get(day);
            if (free == null || !free) {
                return false;
            }
        }
        return true;
    }

    /** Marks nights reserved (unavailable) on the calendar. */
    public void markReserved(LocalDate start, int nights) {
        for (int i = 0; i < nights; i++) {
            availabilityCalendar.put(start.plusDays(i), false);
        }
        incrementVersion();
    }

    /** Frees nights after cancel / checkout. */
    public void markAvailable(LocalDate start, int nights) {
        for (int i = 0; i < nights; i++) {
            availabilityCalendar.put(start.plusDays(i), true);
        }
        incrementVersion();
    }

    public void checkIn() {
        status = RoomStatus.OCCUPIED;
    }

    public void checkOut() {
        status = RoomStatus.BEING_SERVICED;
    }
}
