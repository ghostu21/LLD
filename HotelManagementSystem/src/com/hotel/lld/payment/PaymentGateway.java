package com.hotel.lld.payment;

/** Strategy interface for credit card / check / cash rails. */
public interface PaymentGateway {
    PaymentResult charge(PaymentRequest request);
}
