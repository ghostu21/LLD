package com.carrental.lld.payment;

/**
 * Outcome of a single payment gateway attempt.
 */
public class PaymentResult {
    private final boolean success;
    private final boolean retryable;
    private final String transactionId;
    private final String message;

    private PaymentResult(boolean success, boolean retryable, String transactionId, String message) {
        this.success = success;
        this.retryable = retryable;
        this.transactionId = transactionId;
        this.message = message;
    }

    /** @return successful charge */
    public static PaymentResult success(String transactionId) {
        return new PaymentResult(true, false, transactionId, "Payment succeeded");
    }

    /** @return non-retryable failure */
    public static PaymentResult failure(String message) {
        return new PaymentResult(false, false, null, message);
    }

    /** @return retryable transient failure */
    public static PaymentResult retryableFailure(String message) {
        return new PaymentResult(false, true, null, message);
    }

    /** @return whether charge succeeded */
    public boolean isSuccess() { return success; }

    /** @return whether caller should retry */
    public boolean isRetryable() { return retryable; }

    /** @return gateway transaction id (null on failure) */
    public String getTransactionId() { return transactionId; }

    /** @return status message */
    public String getMessage() { return message; }
}
