package com.hotel.lld.events;

/** Push notification channel subscriber. */
public class PushNotificationService implements HotelEventListener {
    @Override
    public void onEvent(HotelEvent event) {
        System.out.println("[PUSH]  " + event.getType() + " → guest=" + event.getGuestId()
                + " | " + event.getMessage());
    }
}
