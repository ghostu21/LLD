package com.spotify.lld.limits;

/**
 * Classic token-bucket algorithm (package-private helper for {@link RateLimiter}).
 * <p>
 * Why: allows short bursts up to {@code capacity} while sustaining
 * {@code refillPerSecond} over time — smoother than a hard fixed window.
 * <p>
 * Logic: {@link #tryConsume} refills based on elapsed nanos, then spends one
 * token if available. Refill adds {@code elapsed * refillPerSecond} capped at capacity.
 */
class TokenBucket {
    private double tokens;
    private final int capacity;
    private final int refillPerSecond;
    private long lastRefillNanos;

    /**
     * @param capacity         max burst size
     * @param refillPerSecond  steady-state refill rate
     */
    TokenBucket(int capacity, int refillPerSecond) {
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.tokens = capacity;
        this.lastRefillNanos = System.nanoTime();
    }

    /**
     * Thread-safe consume of one token.
     * @return false if no tokens remain after refill
     */
    synchronized boolean tryConsume() {
        refill();
        if (tokens >= 1.0) {
            tokens -= 1.0;
            return true;
        }
        return false;
    }

    /** Adds tokens proportional to time since last refill, capped at capacity. */
    private void refill() {
        long now = System.nanoTime();
        double elapsed = (now - lastRefillNanos) / 1_000_000_000.0;
        tokens = Math.min(capacity, tokens + elapsed * refillPerSecond);
        lastRefillNanos = now;
    }
}
