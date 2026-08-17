package com.hotel.lld.events;

@FunctionalInterface
public interface HotelEventListener {
    void onEvent(HotelEvent event);
}
