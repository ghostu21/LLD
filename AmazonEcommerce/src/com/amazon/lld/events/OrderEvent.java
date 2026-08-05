package com.amazon.lld.events;

import java.time.Instant;

/**
 * Immutable order domain event for pub/sub.
 * <p>
 * Why: decouples order/shipping/return services from notification delivery.
 */
public class OrderEvent {
    private final OrderEventType type;
    private final String orderId;
    private final String memberId;
    private final String payload;
    private final Instant timestamp;

    /**
     * @param type     event kind
     * @param orderId  affected order
     * @param memberId buyer to notify
     * @param payload  human-readable detail
     */
    public OrderEvent(OrderEventType type, String orderId, String memberId, String payload) {
        this.type = type;
        this.orderId = orderId;
        this.memberId = memberId;
        this.payload = payload;
        this.timestamp = Instant.now();
    }

    /** @return event type */
    public OrderEventType getType() { return type; }

    /** @return order id */
    public String getOrderId() { return orderId; }

    /** @return member id */
    public String getMemberId() { return memberId; }

    /** @return detail payload */
    public String getPayload() { return payload; }

    /** @return when event was created */
    public Instant getTimestamp() { return timestamp; }
}
