package com.hotel.lld.payment;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Async payment orchestrator with retry and exponential backoff.
 */
public class PaymentService {
    private static final int MAX_RETRIES = 3;
    private static final long BASE_BACKOFF_MS = 200L;

    private final Map<PaymentMethod, PaymentGateway> gateways = new ConcurrentHashMap<>();

    public void registerGateway(PaymentMethod method, PaymentGateway gateway) {
        gateways.put(method, gateway);
    }

    public CompletableFuture<PaymentResult> processPaymentAsync(PaymentRequest request) {
        return CompletableFuture.supplyAsync(() -> processWithRetry(request));
    }

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
