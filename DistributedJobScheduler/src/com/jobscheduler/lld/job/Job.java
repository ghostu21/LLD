package com.jobscheduler.lld.job;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Durable schedule definition — source of truth in the job store.
 * <p>
 * Indexed by {@code nextRunAt} for due selection; partitioned by {@code shardKey}.
 */
public final class Job {
    private final String jobId;
    private final String tenantId;
    private final String payload;
    private final ScheduleSpec schedule;
    private final OverlapPolicy overlapPolicy;
    private final CatchUpPolicy catchUpPolicy;
    private final RetryPolicy retryPolicy;
    private final int priority;
    private final String shardKey;
    private final Instant createdAt;

    private final AtomicReference<JobStatus> status = new AtomicReference<>(JobStatus.ACTIVE);
    private volatile Instant nextRunAt;
    private volatile Instant lastRunAt;
    private volatile String lastIdempotencyKey;

    public Job(String jobId, String tenantId, String payload, ScheduleSpec schedule,
               OverlapPolicy overlapPolicy, CatchUpPolicy catchUpPolicy,
               RetryPolicy retryPolicy, int priority, String shardKey, Instant nextRunAt) {
        this.jobId = Objects.requireNonNull(jobId);
        this.tenantId = tenantId != null ? tenantId : "default";
        this.payload = payload != null ? payload : "";
        this.schedule = Objects.requireNonNull(schedule);
        this.overlapPolicy = overlapPolicy != null ? overlapPolicy : OverlapPolicy.SKIP;
        this.catchUpPolicy = catchUpPolicy != null ? catchUpPolicy : CatchUpPolicy.SKIP;
        this.retryPolicy = retryPolicy != null ? retryPolicy : RetryPolicy.defaults();
        this.priority = priority;
        this.shardKey = shardKey != null ? shardKey : jobId;
        this.createdAt = Instant.now();
        this.nextRunAt = Objects.requireNonNull(nextRunAt);
    }

    public String getJobId() {
        return jobId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getPayload() {
        return payload;
    }

    public ScheduleSpec getSchedule() {
        return schedule;
    }

    public OverlapPolicy getOverlapPolicy() {
        return overlapPolicy;
    }

    public CatchUpPolicy getCatchUpPolicy() {
        return catchUpPolicy;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }

    public int getPriority() {
        return priority;
    }

    public String getShardKey() {
        return shardKey;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public JobStatus getStatus() {
        return status.get();
    }

    public Instant getNextRunAt() {
        return nextRunAt;
    }

    public Instant getLastRunAt() {
        return lastRunAt;
    }

    public String getLastIdempotencyKey() {
        return lastIdempotencyKey;
    }

    public void setNextRunAt(Instant nextRunAt) {
        this.nextRunAt = nextRunAt;
    }

    public void setLastRunAt(Instant lastRunAt) {
        this.lastRunAt = lastRunAt;
    }

    public void setLastIdempotencyKey(String key) {
        this.lastIdempotencyKey = key;
    }

    public boolean transitionStatus(JobStatus expected, JobStatus next) {
        return status.compareAndSet(expected, next);
    }

    public void forceStatus(JobStatus next) {
        status.set(next);
    }

    /**
     * Stable idempotency key for one scheduled fire: jobId + scheduled fire instant.
     */
    public static String idempotencyKey(String jobId, Instant scheduledFireAt) {
        return jobId + "@" + scheduledFireAt.toEpochMilli();
    }
}
