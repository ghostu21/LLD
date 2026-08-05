package com.amazon.lld.payment;

import java.util.EnumMap;
import java.util.Map;

/**
 * Factory that resolves {@link PaymentStrategy} by {@link PaymentMethodType}.
 * <p>
 * Why: checkout passes a method type enum — factory returns the correct
 * strategy without hardcoding new Payment subclasses in callers.
 */
public class PaymentStrategyFactory {
    private static final Map<PaymentMethodType, PaymentStrategy> STRATEGIES = new EnumMap<>(PaymentMethodType.class);

    static {
        STRATEGIES.put(PaymentMethodType.CREDIT_CARD, new CreditCardPaymentStrategy());
        STRATEGIES.put(PaymentMethodType.BANK_TRANSFER, new BankTransferPaymentStrategy());
    }

    /**
     * @param type payment rail
     * @return strategy implementation
     * @throws IllegalArgumentException if type is unknown
     */
    public static PaymentStrategy get(PaymentMethodType type) {
        PaymentStrategy strategy = STRATEGIES.get(type);
        if (strategy == null) {
            throw new IllegalArgumentException("No strategy for: " + type);
        }
        return strategy;
    }
}
