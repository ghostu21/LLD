package com.spotify.lld.limits;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class StreamLimiter {
    private final int maxConcurrentStreams;
    private final Map<String, AtomicInteger> activeStreams = new ConcurrentHashMap<>();

    public StreamLimiter(int maxConcurrentStreams) {
        this.maxConcurrentStreams = maxConcurrentStreams;
    }

    public boolean tryAcquireStream(String userId) {
        AtomicInteger count = activeStreams.computeIfAbsent(userId, k -> new AtomicInteger(0));
        int current;
        do {
            current = count.get();
            if (current >= maxConcurrentStreams) return false;
        } while (!count.compareAndSet(current, current + 1));
        return true;
    }

    public void releaseStream(String userId) {
        AtomicInteger count = activeStreams.get(userId);
        if (count != null) count.decrementAndGet();
    }

    public int getActiveStreams(String userId) {
        return activeStreams.getOrDefault(userId, new AtomicInteger(0)).get();
    }
}
