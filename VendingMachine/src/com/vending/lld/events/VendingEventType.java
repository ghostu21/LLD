package com.vending.lld.events;

public enum VendingEventType {
    ITEM_SELECTED,
    PAYMENT_RECEIVED,
    INSUFFICIENT_FUNDS,
    DISPENSED,
    REFUNDED,
    OUT_OF_STOCK,
    LOW_STOCK,
    EXPIRED,
    MAINTENANCE,
    PRICE_CHANGED
}
