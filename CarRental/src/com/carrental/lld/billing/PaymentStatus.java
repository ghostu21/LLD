package com.carrental.lld.billing;

/**
 * Payment lifecycle for a bill.
 */
public enum PaymentStatus {
    UNPAID,
    PENDING,
    PAID,
    FAILED,
    REFUNDED
}
