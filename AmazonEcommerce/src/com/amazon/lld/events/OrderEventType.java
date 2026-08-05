package com.amazon.lld.events;

/**
 * Order lifecycle event types for async notification fan-out.
 * <p>
 * Why: subscribers register per type — shipping updates do not spam
 * checkout listeners, etc.
 */
public enum OrderEventType {
    ORDER_PLACED,
    ORDER_SHIPPED,
    ORDER_CANCELED,
    SHIPMENT_UPDATED,
    RETURN_REQUESTED,
    REFUND_COMPLETED
}
