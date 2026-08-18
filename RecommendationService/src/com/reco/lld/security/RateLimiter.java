package com.reco.lld.security;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-key token bucket rate limiter.
 * <p>
 * Why: recommendation endpoints are expensive and a common scrape / privacy
 * probe surface. Unbounded fan-out would also let an attacker enumerate
 * another user's tastes via timing if IDOR slipped through.
 * <p>
 * Logic: each key holds tokens refilled up to {@code capacity} every
 * {@code refillIntervalMs}. {@link #acquire} consumes one token or throws.
 */
public class RateLimiter {
    private final int capacity;
    private final long refillIntervalMs;
    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(int capacity, long refillIntervalMs) {
        if (capacity <= 0 || refillIntervalMs <= 0) {
            throw new IllegalArgumentException("capacity and interval must be positive");
        }
        this.capacity = capacity;
        this.refillIntervalMs = refillIntervalMs;
    }

    public void acquire(String key) {
        if (key == null || key.isBlank()) {
            throw new RateLimitExceededException("Rate limit key required");
        }
        Bucket bucket = buckets.computeIfAbsent(key, k -> new Bucket(capacity, System.currentTimeMillis()));
        synchronized (bucket) {
            long now = System.currentTimeMillis();
            if (now - bucket.windowStart >= refillIntervalMs) {
                bucket.tokens = capacity;
                bucket.windowStart = now;
            }
            if (bucket.tokens <= 0) {
                throw new RateLimitExceededException("Too many requests for " + key);
            }
            bucket.tokens--;
        }
    }

    private static final class Bucket {
        private int tokens;
        private long windowStart;

        private Bucket(int tokens, long windowStart) {
            this.tokens = tokens;
            this.windowStart = windowStart;
        }
    }
}
