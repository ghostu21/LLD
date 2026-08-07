package com.carrental.lld.payment;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Async payment orchestrator with retry and exponential backoff.
 * <p>
 * Why: charging an external gateway must not block reservation threads;
 * transient failures should retry without manual intervention.
 * <p>
 * Logic: {@link #processPaymentAsync} runs on common pool; up to 3 attempts
 * with backoff 500ms × 2^(attempt-1) when result is retryable.
 */
public class PaymentService {
    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 500L;

    private final Map<PaymentMethod, PaymentGateway> gateways = new ConcurrentHashMap<>();

    /**
     * Registers a gateway for a payment method.
     *
     * @param method  payment rail
     * @param gateway implementation
     */
    public void registerGateway(PaymentMethod method, PaymentGateway gateway) {
        gateways.put(method, gateway);
    }

    /**
     * Processes payment asynchronously with retries.
     *
     * @param request charge request
     * @return future completing to final payment result
     */
    public CompletableFuture<PaymentResult> processPaymentAsync(PaymentRequest request) {
        return CompletableFuture.supplyAsync(() -> processWithRetry(request));
    }

    /**
     * Synchronous retry loop (invoked from async worker).
     */
    PaymentResult processWithRetry(PaymentRequest request) {
        PaymentGateway gateway = gateways.get(request.getMethod());
        if (gateway == null) {
            return PaymentResult.failure("No gateway for method: " + request.getMethod());
        }

        PaymentResult lastResult = PaymentResult.failure("No attempts made");
        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            lastResult = gateway.charge(request);
            if (lastResult.isSuccess()) {
                return lastResult;
            }
            if (!lastResult.isRetryable() || attempt == MAX_RETRIES) {
                return lastResult;
            }
            sleepBackoff(attempt);
        }
        return lastResult;
    }

    private void sleepBackoff(int attempt) {
        long delay = BASE_BACKOFF_MS * (1L << (attempt - 1));
        try {
            TimeUnit.MILLISECONDS.sleep(delay);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
