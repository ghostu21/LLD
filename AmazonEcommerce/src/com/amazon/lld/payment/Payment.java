package com.amazon.lld.payment;

import java.util.UUID;

/**
 * Payment value object — amount, status, method, and transaction id.
 * <p>
 * Why: payment is data attached to an order, not a Template Method subclass.
 * Processing behavior lives in {@link PaymentStrategy} implementations.
 * <p>
 * Logic: created PENDING; strategy updates status and transactionId on success/fail.
 */
public class Payment {
    private final String paymentId;
    private final double amount;
    private PaymentStatus status;
    private final PaymentMethodType methodType;
    private String transactionId;

    /**
     * @param amount     charge amount
     * @param methodType payment rail
     */
    public Payment(double amount, PaymentMethodType methodType) {
        this.paymentId = UUID.randomUUID().toString();
        this.amount = amount;
        this.methodType = methodType;
        this.status = PaymentStatus.PENDING;
    }

    /** @return internal payment id */
    public String getPaymentId() { return paymentId; }

    /** @return charge amount */
    public double getAmount() { return amount; }

    /** @return current status */
    public PaymentStatus getStatus() { return status; }

    /** @return payment method */
    public PaymentMethodType getMethodType() { return methodType; }

    /** @return processor transaction id (set after pay) */
    public String getTransactionId() { return transactionId; }

    /** Updates status (used by strategies and refund flow). */
    public void setStatus(PaymentStatus status) { this.status = status; }

    /** Sets external transaction reference after successful charge. */
    public void setTransactionId(String transactionId) { this.transactionId = transactionId; }
}
