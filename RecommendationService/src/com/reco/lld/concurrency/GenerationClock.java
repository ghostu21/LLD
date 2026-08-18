package com.reco.lld.concurrency;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monotonic generations so a slate computed before a write cannot be cached
 * as if it were current.
 * <p>
 * Why: classic race is recommend-compute → hide → cache.put(stale).
 * Cache keys include these counters so a late put lands on a dead key.
 */
public final class GenerationClock {
    private final AtomicLong catalog = new AtomicLong(1);
    private final ConcurrentHashMap<String, AtomicLong> users = new ConcurrentHashMap<>();

    public long user(String userId) {
        return users.computeIfAbsent(userId, k -> new AtomicLong(1)).get();
    }

    public void bumpUser(String userId) {
        users.computeIfAbsent(userId, k -> new AtomicLong(1)).incrementAndGet();
    }

    public long catalog() {
        return catalog.get();
    }

    public void bumpCatalog() {
        catalog.incrementAndGet();
    }
}
