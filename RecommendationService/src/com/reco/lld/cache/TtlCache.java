package com.reco.lld.cache;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CompletionException;
import java.util.function.Supplier;

/**
 * Tiny TTL cache (stand-in for Redis) with single-flight loads.
 * <p>
 * Why: identical homepage requests within a minute should not re-score
 * the full catalog. Concurrent misses on the same key share one compute
 * (cache stampede / thundering herd).
 */
public class TtlCache<V> {
    private final Duration ttl;
    private final ConcurrentHashMap<String, Entry<V>> data = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<V>> inflight = new ConcurrentHashMap<>();

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

    /**
     * Returns a cached value or runs {@code loader} once per key while others wait.
     */
    public V getOrCompute(String key, Supplier<V> loader) {
        V hit = get(key);
        if (hit != null) return hit;

        CompletableFuture<V> mine = new CompletableFuture<>();
        CompletableFuture<V> winner = inflight.putIfAbsent(key, mine);
        if (winner == null) {
            try {
                V loaded = loader.get();
                put(key, loaded);
                mine.complete(loaded);
                return loaded;
            } catch (RuntimeException e) {
                mine.completeExceptionally(e);
                throw e;
            } catch (Exception e) {
                mine.completeExceptionally(e);
                throw new CompletionException(e);
            } finally {
                inflight.remove(key, mine);
            }
        }
        try {
            return winner.join();
        } catch (CompletionException e) {
            if (e.getCause() instanceof RuntimeException runtime) throw runtime;
            throw e;
        }
    }

    public void invalidatePrefix(String prefix) {
        data.keySet().removeIf(k -> k.startsWith(prefix));
    }

    public void clear() {
        data.clear();
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
