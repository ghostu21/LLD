package com.vending.lld.events;

/**
 * Hardware display — one of several listeners, not the only observer.
 */
public final class DisplayListener implements VendingEventListener {
    @Override
    public void onEvent(VendingEvent event) {
        System.out.println("Display: " + event.getMessage());
    }
}
