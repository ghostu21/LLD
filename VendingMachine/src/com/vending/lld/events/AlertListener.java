package com.vending.lld.events;

/**
 * Ops alert on low / out-of-stock (could be SMS in prod).
 */
public final class AlertListener implements VendingEventListener {
    @Override
    public void onEvent(VendingEvent event) {
        if (event.getType() == VendingEventType.LOW_STOCK
                || event.getType() == VendingEventType.OUT_OF_STOCK
                || event.getType() == VendingEventType.EXPIRED) {
            System.out.println("Alert: " + event.getMessage());
        }
    }
}
