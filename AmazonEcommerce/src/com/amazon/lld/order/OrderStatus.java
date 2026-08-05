package com.amazon.lld.order;

/**
 * Fulfillment and post-sale states for an order.
 * <p>
 * Why: cancel is allowed only before SHIPPED; returns add RETURN_* and REFUND_APPLIED.
 */
public enum OrderStatus {
    PENDING,
    UNSHIPPED,
    SHIPPED,
    DELIVERED,
    COMPLETED,
    CANCELED,
    RETURN_REQUESTED,
    RETURNED,
    REFUND_APPLIED,
    ERROR
}
