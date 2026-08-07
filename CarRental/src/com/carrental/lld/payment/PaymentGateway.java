package com.carrental.lld.payment;

/**
 * Abstraction over external payment processors.
 * <p>
 * Why: card and bank rails differ; the service selects an implementation by
 * {@link PaymentMethod} without branching on gateway details everywhere.
 */
public interface PaymentGateway {
    /**
     * Attempts a single charge.
     *
     * @param request payment payload
     * @return success or failure (possibly retryable)
     */
    PaymentResult charge(PaymentRequest request);
}
