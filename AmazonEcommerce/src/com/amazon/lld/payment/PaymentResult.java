package com.amazon.lld.payment;

/**
 * Outcome of a {@link PaymentStrategy#pay} invocation.
 * <p>
 * Why: strategies return structured success/failure without throwing for
 * expected declines (demo card failures, etc.).
 */
public class PaymentResult {
    private final boolean success;
    private final String transactionId;
    private final String message;

    /**
     * @param success       whether charge succeeded
     * @param transactionId processor reference (may be null on failure)
     * @param message       human-readable detail
     */
    public PaymentResult(boolean success, String transactionId, String message) {
        this.success = success;
        this.transactionId = transactionId;
        this.message = message;
    }

    /** @return true if payment completed */
    public boolean isSuccess() { return success; }

    /** @return transaction id from processor */
    public String getTransactionId() { return transactionId; }

    /** @return detail message */
    public String getMessage() { return message; }
}
