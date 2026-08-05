package com.amazon.lld.payment;

import java.util.UUID;

/**
 * Credit card payment strategy (demo stub).
 * <p>
 * Why: one Strategy per rail — not a Payment subclass duplicate.
 * <p>
 * Logic: simulates instant authorization; sets COMPLETED and a txn id on success.
 */
public class CreditCardPaymentStrategy implements PaymentStrategy {

    /**
     * Charges via simulated card network.
     */
    @Override
    public PaymentResult pay(Payment payment) {
        String txnId = "CC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        payment.setTransactionId(txnId);
        payment.setStatus(PaymentStatus.COMPLETED);
        return new PaymentResult(true, txnId, "Credit card authorized for $" + payment.getAmount());
    }
}
