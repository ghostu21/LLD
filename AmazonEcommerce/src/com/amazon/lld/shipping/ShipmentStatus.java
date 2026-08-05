package com.amazon.lld.shipping;

/**
 * Carrier-reported package lifecycle states.
 * <p>
 * Why: {@link com.amazon.lld.shipping.ShipmentPoller} polls trackers and maps
 * API responses to these enums for order updates.
 */
public enum ShipmentStatus {
    LABEL_CREATED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    EXCEPTION
}
