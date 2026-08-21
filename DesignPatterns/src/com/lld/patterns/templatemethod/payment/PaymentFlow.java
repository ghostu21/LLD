package com.lld.patterns.templatemethod.payment;

/**
 * Abstract class: {@link #sendMoney()} is the template (final skeleton).
 * Subclasses fill abstract steps; they may override the OTP hook.
 */
public abstract class PaymentFlow {

    public abstract void validateRequest();

    public abstract void debitAmount();

    public abstract void calculateFees();

    public abstract void creditAmount();

    public final void sendMoney() {
        validateRequest();
        debitAmount();
        calculateFees();
        creditAmount();
    }

    protected boolean requiresOTPAuthentication() {
        return false;
    }

    public void logTransaction() {
        System.out.println("Transaction Completed!");
    }
}
