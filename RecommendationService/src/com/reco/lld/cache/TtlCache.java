package com.reco.lld.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tiny TTL cache (stand-in for Redis).
 * <p>
 * Why: identical homepage requests within a minute should not re-score
 * the full catalog. ConcurrentHashMap keeps demo traffic thread-safe.
 */
public class TtlCache<V> {
    private final Duration ttl;
    private final ConcurrentHashMap<String, Entry<V>> data = new ConcurrentHashMap<>();

    public TtlCache(Duration ttl) {
        this.ttl = ttl;
    }

    public V get(String key) {
        Entry<V> entry = data.get(key);
        if (entry == null) return null;
        if (Instant.now().isAfter(entry.expiresAt)) {
            data.remove(key, entry);
            return null;
        }
        return entry.value;
    }

    public void put(String key, V value) {
        data.put(key, new Entry<>(value, Instant.now().plus(ttl)));
    }

    public void invalidatePrefix(String prefix) {
        data.keySet().removeIf(k -> k.startsWith(prefix));
    }

    private static final class Entry<V> {
        final V value;
        final Instant expiresAt;

        Entry(V value, Instant expiresAt) {
            this.value = value;
            this.expiresAt = expiresAt;
        }
    }
}
