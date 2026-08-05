package com.amazon.lld.payment;

/**
 * Strategy interface for executing a payment on a given rail.
 * <p>
 * Why: credit card vs bank transfer have different steps — Strategy keeps
 * {@link PaymentProcessor} free of hardcoded if/else chains.
 */
public interface PaymentStrategy {
    /**
     * Attempts to charge the payment.
     *
     * @param payment mutable payment value object (status/transactionId updated on success)
     * @return structured result
     */
    PaymentResult pay(Payment payment);
}
