package com.hotel.lld.events;

/** Domain event payload published on the async bus. */
public class HotelEvent {
    private final HotelEventType type;
    private final String reservationNumber;
    private final String guestId;
    private final String roomNumber;
    private final String message;

    public HotelEvent(HotelEventType type, String reservationNumber,
                      String guestId, String roomNumber, String message) {
        this.type = type;
        this.reservationNumber = reservationNumber;
        this.guestId = guestId;
        this.roomNumber = roomNumber;
        this.message = message;
    }

    public HotelEventType getType() {
        return type;
    }

    public String getReservationNumber() {
        return reservationNumber;
    }

    public String getGuestId() {
        return guestId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public String getMessage() {
        return message;
    }
}
