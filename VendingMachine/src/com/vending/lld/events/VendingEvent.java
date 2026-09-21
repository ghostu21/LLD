package com.vending.lld.events;

public final class VendingEvent {
    private final VendingEventType type;
    private final String message;

    public VendingEvent(VendingEventType type, String message) {
        this.type = type;
        this.message = message;
    }

    public VendingEventType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }
}
