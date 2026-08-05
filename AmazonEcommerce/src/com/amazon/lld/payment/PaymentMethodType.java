package com.amazon.lld.payment;

/**
 * Supported payment rails at checkout.
 * <p>
 * Why: {@link com.amazon.lld.payment.PaymentStrategyFactory} maps each type
 * to a {@link PaymentStrategy} implementation (Strategy + Factory pattern).
 */
public enum PaymentMethodType {
    CREDIT_CARD,
    BANK_TRANSFER
}
