package com.spotify.lld.limits;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Per-account concurrent stream cap using CAS (compare-and-swap).
 * <p>
 * Why: without limits, a stolen account can stream on thousands of devices.
 * Free tiers often allow 1 concurrent stream; premium allows more.
 * <p>
 * Logic of {@link #tryAcquireStream}: read current count; if at max return false;
 * else CAS current→current+1 and retry on contention so two devices cannot both
 * sneak under the limit. {@link #releaseStream} decrements when playback ends.
 */
public class StreamLimiter {
    private final int maxConcurrentStreams;
    /** userId → active stream count. */
    private final Map<String, AtomicInteger> activeStreams = new ConcurrentHashMap<>();

    public StreamLimiter(int maxConcurrentStreams) {
        this.maxConcurrentStreams = maxConcurrentStreams;
    }

    /**
     * Attempts to reserve one stream slot for {@code userId}.
     * @return true if acquired; false if already at the max
     */
    public boolean tryAcquireStream(String userId) {
        AtomicInteger count = activeStreams.computeIfAbsent(userId, k -> new AtomicInteger(0));
        int current;
        do {
            current = count.get();
            if (current >= maxConcurrentStreams) return false;
        } while (!count.compareAndSet(current, current + 1));
        return true;
    }

    /** Releases one previously acquired stream slot. */
    public void releaseStream(String userId) {
        AtomicInteger count = activeStreams.get(userId);
        if (count != null) count.decrementAndGet();
    }

    /** Observability helper for demos/tests. */
    public int getActiveStreams(String userId) {
        return activeStreams.getOrDefault(userId, new AtomicInteger(0)).get();
    }
}
