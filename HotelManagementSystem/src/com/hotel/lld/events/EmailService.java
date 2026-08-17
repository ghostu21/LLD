package com.hotel.lld.events;

/** Email channel subscriber. */
public class EmailService implements HotelEventListener {
    @Override
    public void onEvent(HotelEvent event) {
        System.out.println("[EMAIL] " + event.getType() + " → guest=" + event.getGuestId()
                + " booking=" + event.getReservationNumber() + " | " + event.getMessage());
    }
}
