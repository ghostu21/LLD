package com.gdrive.lld.quota;

import com.gdrive.lld.account.User;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Per-user storage cap. Usage is an {@link AtomicLong} so concurrent writes
 * cannot both squeeze past the limit.
 */
public final class QuotaService {
    public static final long DEFAULT_LIMIT_BYTES = 10_000L;

    private final ConcurrentHashMap<String, AtomicLong> used = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Long> limits = new ConcurrentHashMap<>();

    public void setLimit(User user, long bytes) {
        limits.put(user.getUsername(), bytes);
    }

    public long used(User user) {
        return used.computeIfAbsent(user.getUsername(), k -> new AtomicLong()).get();
    }

    public long limit(User user) {
        return limits.getOrDefault(user.getUsername(), DEFAULT_LIMIT_BYTES);
    }

    public void applyDelta(User owner, long deltaBytes) {
        AtomicLong counter = used.computeIfAbsent(owner.getUsername(), k -> new AtomicLong());
        if (deltaBytes <= 0) {
            counter.addAndGet(deltaBytes);
            return;
        }
        while (true) {
            long current = counter.get();
            long next = current + deltaBytes;
            if (next > limit(owner)) {
                throw new QuotaExceededException(
                        "Quota exceeded for " + owner.getUsername()
                                + " used=" + current + " delta=" + deltaBytes
                                + " limit=" + limit(owner));
            }
            if (counter.compareAndSet(current, next)) {
                return;
            }
        }
    }
}
