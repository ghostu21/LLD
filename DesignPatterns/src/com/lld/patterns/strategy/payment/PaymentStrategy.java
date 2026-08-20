package com.lld.patterns.strategy.payment;

/**
 * Strategy contract for checkout. Each payment rail is a class, not a switch arm.
 */
public interface PaymentStrategy {
    void pay(double amount);
}
