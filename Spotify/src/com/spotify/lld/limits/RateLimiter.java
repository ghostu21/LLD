package com.spotify.lld.limits;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RateLimiter {
    private final int requestsPerSecond;
    private final Map<String, TokenBucket> buckets = new ConcurrentHashMap<>();

    public RateLimiter(int requestsPerSecond) {
        this.requestsPerSecond = requestsPerSecond;
    }

    public boolean tryConsume(String userId) {
        TokenBucket bucket = buckets.computeIfAbsent(userId,
                k -> new TokenBucket(requestsPerSecond * 2, requestsPerSecond));
        return bucket.tryConsume();
    }
}
