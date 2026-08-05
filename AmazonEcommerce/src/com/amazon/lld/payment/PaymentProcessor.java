package com.amazon.lld.payment;

/**
 * Orchestrates payment execution via Strategy + Factory.
 * <p>
 * Why: single entry point for checkout — resolves strategy from factory,
 * runs pay, returns updated Payment.
 * <p>
 * Logic: create Payment → factory.get(method) → strategy.pay → return payment.
 */
public class PaymentProcessor {

    /**
     * Processes a charge for the given amount and method.
     *
     * @param amount     total to charge
     * @param methodType payment rail
     * @return payment with final status and transaction id
     */
    public Payment process(double amount, PaymentMethodType methodType) {
        Payment payment = new Payment(amount, methodType);
        PaymentStrategy strategy = PaymentStrategyFactory.get(methodType);
        PaymentResult result = strategy.pay(payment);
        if (!result.isSuccess()) {
            payment.setStatus(PaymentStatus.FAILED);
        }
        return payment;
    }
}
