package com.hotel.lld.events;

/** SMS channel subscriber. */
public class SMSService implements HotelEventListener {
    @Override
    public void onEvent(HotelEvent event) {
        System.out.println("[SMS]   " + event.getType() + " → guest=" + event.getGuestId()
                + " room=" + event.getRoomNumber() + " | " + event.getMessage());
    }
}
