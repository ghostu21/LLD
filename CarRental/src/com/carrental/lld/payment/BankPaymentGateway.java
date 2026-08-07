package com.carrental.lld.payment;

/**
 * Stub bank transfer payment gateway.
 * <p>
 * Logic: always succeeds immediately in demo.
 */
public class BankPaymentGateway implements PaymentGateway {
    @Override
    public PaymentResult charge(PaymentRequest request) {
        return PaymentResult.success("BANK-TXN-" + System.currentTimeMillis());
    }
}
