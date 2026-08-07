package com.carrental.lld.payment;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Stub card payment gateway with configurable transient failures for demo.
 * <p>
 * Logic: first two attempts return retryable failure; third succeeds.
 */
public class CardPaymentGateway implements PaymentGateway {
    private final AtomicInteger attemptCounter = new AtomicInteger();

    @Override
    public PaymentResult charge(PaymentRequest request) {
        int attempt = attemptCounter.incrementAndGet();
        if (attempt < 3) {
            return PaymentResult.retryableFailure("Card gateway timeout (attempt " + attempt + ")");
        }
        return PaymentResult.success("CARD-TXN-" + System.currentTimeMillis());
    }

    /** Resets demo attempt counter between scenarios. */
    public void reset() {
        attemptCounter.set(0);
    }
}
