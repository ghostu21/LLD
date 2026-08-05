package com.spotify.lld.limits;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Per-user API rate limiter backed by token buckets.
 * <p>
 * Why: concurrent stream caps stop multi-device abuse; rate limits stop a
 * single client from flooding the API.
 * <p>
 * Logic: each user gets a {@link TokenBucket} with capacity = 2×RPS (burst)
 * and refill = RPS. {@link #tryConsume} returns false when the bucket is empty.
 */
public class RateLimiter {
    private final int requestsPerSecond;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(int requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }

    /**
     * Consumes one token for {@code userId} if available.
     * @return true if the request is allowed
     */
    public boolean tryConsume(String userId) {
        TokenBucket bucket = buckets.computeIfAbsent(userId,
                k -> new TokenBucket(requestsPerSecond * 2, requestsPerSecond));
        return bucket.tryConsume();
    }
}
