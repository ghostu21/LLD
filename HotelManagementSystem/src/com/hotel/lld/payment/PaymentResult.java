package com.hotel.lld.payment;

/** Outcome of a payment attempt. */
public class PaymentResult {
    private final boolean success;
    private final boolean retryable;
    private final String message;
    private final PaymentStatus status;

    private PaymentResult(boolean success, boolean retryable, String message, PaymentStatus status) {
        this.success = success;
        this.retryable = retryable;
        this.message = message;
        this.status = status;
    }

    public static PaymentResult success(String message) {
        return new PaymentResult(true, false, message, PaymentStatus.COMPLETED);
    }

    public static PaymentResult failure(String message) {
        return new PaymentResult(false, false, message, PaymentStatus.FAILED);
    }

    public static PaymentResult retryableFailure(String message) {
        return new PaymentResult(false, true, message, PaymentStatus.PENDING);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isRetryable() {
        return retryable;
    }

    public String getMessage() {
        return message;
    }

    public PaymentStatus getStatus() {
        return status;
    }
}
