package com.jobscheduler.lld.job;

import java.time.Duration;

/**
 * Exponential backoff for failed executions before DLQ.
 */
public final class RetryPolicy {
    private final int maxAttempts;
    private final Duration initialBackoff;
    private final Duration maxBackoff;
    private final double multiplier;

    public RetryPolicy(int maxAttempts, Duration initialBackoff, Duration maxBackoff, double multiplier) {
        if (maxAttempts < 1) {
            throw new IllegalArgumentException("maxAttempts must be >= 1");
        }
        this.maxAttempts = maxAttempts;
        this.initialBackoff = initialBackoff;
        this.maxBackoff = maxBackoff;
        this.multiplier = multiplier;
    }

    public static RetryPolicy defaults() {
        return new RetryPolicy(3, Duration.ofSeconds(1), Duration.ofMinutes(1), 2.0);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public Duration backoffForAttempt(int attempt) {
        if (attempt <= 1) {
            return initialBackoff;
        }
        double millis = initialBackoff.toMillis() * Math.pow(multiplier, attempt - 1);
        long capped = Math.min((long) millis, maxBackoff.toMillis());
        return Duration.ofMillis(capped);
    }
}
