package com.jobscheduler.lld.schedule;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 * Hierarchical-style timing wheel for near-term due jobs (1s slots, 1h horizon).
 * <p>
 * Why: scanning the full DB every second does not scale. Near-term jobs live
 * in memory; far-horizon jobs stay in the durable store until paged into the wheel.
 */
public final class TimeWheel {
    private final int slotCount;
    private final Duration slotDuration;
    private final ConcurrentMap<Integer, ConcurrentLinkedQueue<String>> slots;
    private volatile Instant wheelStart;
    private volatile int currentSlot;

    public TimeWheel(int slotCount, Duration slotDuration) {
        this.slotCount = slotCount;
        this.slotDuration = Objects.requireNonNull(slotDuration);
        this.slots = new ConcurrentHashMap<>();
        for (int i = 0; i < slotCount; i++) {
            slots.put(i, new ConcurrentLinkedQueue<>());
        }
        this.wheelStart = Instant.now().truncatedTo(java.time.temporal.ChronoUnit.SECONDS);
        this.currentSlot = 0;
    }

    public static TimeWheel oneHourSecondResolution() {
        return new TimeWheel(3600, Duration.ofSeconds(1));
    }

    public Duration horizon() {
        return slotDuration.multipliedBy(slotCount);
    }

    /**
     * @return true if scheduled within the wheel horizon and inserted
     */
    public boolean offer(String jobId, Instant runAt, Instant now) {
        long delayMs = Duration.between(now, runAt).toMillis();
        if (delayMs < 0) {
            delayMs = 0;
        }
        long horizonMs = horizon().toMillis();
        if (delayMs >= horizonMs) {
            return false;
        }
        int offset = (int) (delayMs / slotDuration.toMillis());
        int slot = (currentSlot + offset) % slotCount;
        slots.get(slot).offer(jobId);
        return true;
    }

    /**
     * Advance the wheel to {@code now} and collect due job ids (may include stale ids).
     */
    public List<String> advanceTo(Instant now) {
        List<String> due = new ArrayList<>();
        long elapsed = Duration.between(wheelStart, now).toMillis();
        if (elapsed < 0) {
            return due;
        }
        int steps = (int) (elapsed / slotDuration.toMillis());
        if (steps <= 0) {
            // still drain current slot for late inserts
            drainSlot(currentSlot, due);
            return due;
        }
        steps = Math.min(steps, slotCount); // cap catch-up drain
        for (int i = 0; i < steps; i++) {
            drainSlot(currentSlot, due);
            currentSlot = (currentSlot + 1) % slotCount;
            wheelStart = wheelStart.plus(slotDuration);
        }
        return due;
    }

    private void drainSlot(int slot, List<String> out) {
        ConcurrentLinkedQueue<String> q = slots.get(slot);
        String id;
        while ((id = q.poll()) != null) {
            out.add(id);
        }
    }

    public void clear() {
        for (ConcurrentLinkedQueue<String> q : slots.values()) {
            q.clear();
        }
    }

    public int getCurrentSlot() {
        return currentSlot;
    }
}
