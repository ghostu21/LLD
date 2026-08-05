package com.amazon.lld.order;

import java.time.Instant;

/**
 * Audit log entry for an order status transition.
 * <p>
 * Why: support and dispute resolution need a timestamped trail of what happened.
 */
public class OrderLog {
    private final Instant timestamp;
    private final OrderStatus status;
    private final String message;

    /**
     * @param status  status at time of log
     * @param message human-readable detail
     */
    public OrderLog(OrderStatus status, String message) {
        this.timestamp = Instant.now();
        this.status = status;
        this.message = message;
    }

    /** @return when the event was recorded */
    public Instant getTimestamp() { return timestamp; }

    /** @return order status */
    public OrderStatus getStatus() { return status; }

    /** @return log message */
    public String getMessage() { return message; }

    @Override
    public String toString() {
        return timestamp + " [" + status + "] " + message;
    }
}
