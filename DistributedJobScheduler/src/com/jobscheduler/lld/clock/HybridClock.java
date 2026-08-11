package com.jobscheduler.lld.clock;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Hybrid logical clock to tolerate wall-clock skew across workers.
 * <p>
 * Why: distributed schedulers must not trust {@code Instant.now()} alone —
 * a slow clock would miss due jobs; a fast clock would fire early and
 * duplicate under failover. We combine physical time with a logical counter
 * and apply a skew tolerance window when deciding "due".
 */
public final class HybridClock {
    private final Duration skewTolerance;
    private final AtomicLong logical = new AtomicLong(0);
    private volatile Instant lastPhysical = Instant.EPOCH;

    public HybridClock(Duration skewTolerance) {
        this.skewTolerance = skewTolerance != null ? skewTolerance : Duration.ofSeconds(2);
    }

    public static HybridClock defaults() {
        return new HybridClock(Duration.ofSeconds(2));
    }

    /**
     * Current hybrid timestamp: max(wall, last) + logical tick.
     */
    public Instant now() {
        Instant wall = Instant.now();
        synchronized (this) {
            if (wall.isAfter(lastPhysical)) {
                lastPhysical = wall;
                logical.set(0);
                return wall;
            }
            long tick = logical.incrementAndGet();
            return lastPhysical.plusMillis(tick);
        }
    }

    /**
     * A job is due if scheduledFireAt &lt;= now + skewTolerance
     * (allows slightly early claim to absorb clock drift).
     */
    public boolean isDue(Instant scheduledFireAt) {
        Instant cutoff = now().plus(skewTolerance);
        return !scheduledFireAt.isAfter(cutoff);
    }

    /**
     * Lateness of a firing (positive = late). Negative within skew is drift noise.
     */
    public Duration lateness(Instant scheduledFireAt, Instant actualFireAt) {
        return Duration.between(scheduledFireAt, actualFireAt);
    }

    public Duration getSkewTolerance() {
        return skewTolerance;
    }
}
