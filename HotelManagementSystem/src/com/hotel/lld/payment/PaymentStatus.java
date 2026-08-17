package com.hotel.lld.payment;

/** Payment lifecycle states. */
public enum PaymentStatus {
    UNPAID, PENDING, COMPLETED, FAILED, DECLINED, CANCELLED, REFUNDED
}
