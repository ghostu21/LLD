package com.amazon.lld.payment;

import java.util.UUID;

/**
 * Bank transfer (ACH/wire) payment strategy (demo stub).
 * <p>
 * Why: bank transfers are asynchronous in production — demo marks COMPLETED
 * immediately with a transfer reference.
 */
public class BankTransferPaymentStrategy implements PaymentStrategy {

    /**
     * Initiates simulated bank transfer.
     */
    @Override
    public PaymentResult pay(Payment payment) {
        String txnId = "ACH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        payment.setTransactionId(txnId);
        payment.setStatus(PaymentStatus.COMPLETED);
        return new PaymentResult(true, txnId, "Bank transfer initiated for $" + payment.getAmount());
    }
}
