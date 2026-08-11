package com.jobscheduler.lld.schedule;

import com.jobscheduler.lld.clock.HybridClock;
import com.jobscheduler.lld.job.CatchUpPolicy;
import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.JobStatus;
import com.jobscheduler.lld.job.JobType;
import com.jobscheduler.lld.job.OverlapPolicy;
import com.jobscheduler.lld.job.ScheduleSpec;
import com.jobscheduler.lld.store.ExecutionStore;
import com.jobscheduler.lld.store.JobStore;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Per-shard due scanner: loads near-term jobs into a {@link TimeWheel},
 * advances the wheel, applies overlap + catch-up policies, emits fire intents.
 * <p>
 * Separates "when to run" from "what to run" (workers execute payloads).
 */
public final class DueScanner {
    public static final class FireIntent {
        private final String jobId;
        private final Instant scheduledFireAt;
        private final boolean catchUp;

        public FireIntent(String jobId, Instant scheduledFireAt, boolean catchUp) {
            this.jobId = jobId;
            this.scheduledFireAt = scheduledFireAt;
            this.catchUp = catchUp;
        }

        public String getJobId() {
            return jobId;
        }

        public Instant getScheduledFireAt() {
            return scheduledFireAt;
        }

        public boolean isCatchUp() {
            return catchUp;
        }
    }

    private final int shardId;
    private final int shardCount;
    private final JobStore jobStore;
    private final ExecutionStore executionStore;
    private final HybridClock clock;
    private final TimeWheel wheel;
    private final Duration catchUpWindow;
    private final Duration jitterMax;

    public DueScanner(int shardId, int shardCount, JobStore jobStore,
                      ExecutionStore executionStore, HybridClock clock,
                      Duration catchUpWindow, Duration jitterMax) {
        this.shardId = shardId;
        this.shardCount = shardCount;
        this.jobStore = Objects.requireNonNull(jobStore);
        this.executionStore = Objects.requireNonNull(executionStore);
        this.clock = Objects.requireNonNull(clock);
        this.wheel = TimeWheel.oneHourSecondResolution();
        this.catchUpWindow = catchUpWindow != null ? catchUpWindow : Duration.ofHours(1);
        this.jitterMax = jitterMax != null ? jitterMax : Duration.ofMillis(200);
    }

    public int getShardId() {
        return shardId;
    }

    /** Cold-start / refresh: page active jobs into the wheel. */
    public void reloadWheel(Instant now) {
        wheel.clear();
        for (Job job : jobStore.findActiveInShard(shardId, shardCount)) {
            Instant withJitter = applyJitter(job.getNextRunAt());
            wheel.offer(job.getJobId(), withJitter, now);
        }
    }

    public List<FireIntent> tick(Instant now) {
        List<FireIntent> fires = new ArrayList<>();
        // Also pick DB-due jobs not yet in wheel (far → near handoff)
        for (Job job : jobStore.findDueInShard(shardId, shardCount, now.plus(clock.getSkewTolerance()), 500)) {
            wheel.offer(job.getJobId(), job.getNextRunAt(), now);
        }

        List<String> dueIds = wheel.advanceTo(now);
        for (String jobId : dueIds) {
            jobStore.findById(jobId).ifPresent(job -> {
                if (job.getStatus() != JobStatus.ACTIVE) {
                    return;
                }
                if (!clock.isDue(job.getNextRunAt())) {
                    wheel.offer(jobId, job.getNextRunAt(), now);
                    return;
                }
                List<FireIntent> intents = planFires(job, now);
                fires.addAll(intents);
                rearm(job, now);
                if (job.getStatus() == JobStatus.ACTIVE && job.getNextRunAt() != null) {
                    wheel.offer(job.getJobId(), applyJitter(job.getNextRunAt()), now);
                }
            });
        }
        return fires;
    }

    private List<FireIntent> planFires(Job job, Instant now) {
        List<FireIntent> intents = new ArrayList<>();
        Instant scheduled = job.getNextRunAt();

        if (job.getOverlapPolicy() == OverlapPolicy.SKIP
                && executionStore.hasActiveRun(job.getJobId())) {
            return intents;
        }

        // Catch-up for recurring jobs that fell behind
        if (job.getSchedule().getType() == JobType.RECURRING
                && job.getCatchUpPolicy() != CatchUpPolicy.SKIP
                && scheduled.isBefore(now.minus(Duration.ofSeconds(1)))) {
            intents.addAll(catchUpFires(job, scheduled, now));
        } else {
            intents.add(new FireIntent(job.getJobId(), scheduled, false));
        }
        return intents;
    }

    private List<FireIntent> catchUpFires(Job job, Instant from, Instant now) {
        List<FireIntent> intents = new ArrayList<>();
        ScheduleSpec spec = job.getSchedule();
        CronExpression cron = new CronExpression(spec.getCronExpr().orElseThrow());
        Instant cursor = from;
        Instant windowStart = now.minus(catchUpWindow);
        int count = 0;
        while (!cursor.isAfter(now) && count < 100) {
            if (!cursor.isBefore(windowStart)) {
                intents.add(new FireIntent(job.getJobId(), cursor, true));
                count++;
                if (job.getCatchUpPolicy() == CatchUpPolicy.ONE) {
                    break;
                }
            }
            cursor = cron.nextAfter(cursor, spec.getTimezone());
        }
        if (intents.isEmpty()) {
            intents.add(new FireIntent(job.getJobId(), from, false));
        }
        return intents;
    }

    private void rearm(Job job, Instant now) {
        ScheduleSpec spec = job.getSchedule();
        job.setLastRunAt(now);
        if (spec.getType() == JobType.ONE_OFF) {
            job.forceStatus(JobStatus.COMPLETED);
            job.setNextRunAt(Instant.MAX);
            return;
        }
        CronExpression cron = new CronExpression(spec.getCronExpr().orElseThrow());
        Instant next = cron.nextAfter(now, spec.getTimezone());
        job.setNextRunAt(next);
    }

    private Instant applyJitter(Instant runAt) {
        if (jitterMax.isZero() || jitterMax.isNegative()) {
            return runAt;
        }
        long jitter = ThreadLocalRandom.current().nextLong(jitterMax.toMillis() + 1);
        return runAt.plusMillis(jitter);
    }
}
