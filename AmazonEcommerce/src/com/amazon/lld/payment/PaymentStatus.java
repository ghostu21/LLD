package com.amazon.lld.payment;

/**
 * Lifecycle state of a payment or refund transaction.
 * <p>
 * Why: orders and returns track payment/refund progress separately from
 * order fulfillment status.
 */
public enum PaymentStatus {
    PENDING,
    COMPLETED,
    FAILED,
    REFUND_INITIATED,
    REFUNDED
}
