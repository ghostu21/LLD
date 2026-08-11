package com.jobscheduler.lld.job;

import java.time.Instant;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

/**
 * One concrete firing / attempt of a job.
 * <p>
 * Deduped by {@code idempotencyKey}; leased by workers with a TTL so crashes
 * do not lose work.
 */
public final class JobExecution {
    private final String executionId;
    private final String jobId;
    private final String idempotencyKey;
    private final Instant scheduledFireAt;
    private final Instant createdAt;

    private final AtomicReference<ExecutionStatus> status =
            new AtomicReference<>(ExecutionStatus.LEASED);
    private volatile String workerId;
    private volatile Instant leaseExpiresAt;
    private volatile Instant startedAt;
    private volatile Instant finishedAt;
    private volatile int attempt;
    private volatile String errorMessage;
    private volatile long fencingToken;

    public JobExecution(String executionId, String jobId, String idempotencyKey,
                        Instant scheduledFireAt, int attempt, long fencingToken) {
        this.executionId = Objects.requireNonNull(executionId);
        this.jobId = Objects.requireNonNull(jobId);
        this.idempotencyKey = Objects.requireNonNull(idempotencyKey);
        this.scheduledFireAt = Objects.requireNonNull(scheduledFireAt);
        this.createdAt = Instant.now();
        this.attempt = attempt;
        this.fencingToken = fencingToken;
    }

    public String getExecutionId() {
        return executionId;
    }

    public String getJobId() {
        return jobId;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public Instant getScheduledFireAt() {
        return scheduledFireAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public ExecutionStatus getStatus() {
        return status.get();
    }

    public String getWorkerId() {
        return workerId;
    }

    public Instant getLeaseExpiresAt() {
        return leaseExpiresAt;
    }

    public Instant getStartedAt() {
        return startedAt;
    }

    public Instant getFinishedAt() {
        return finishedAt;
    }

    public int getAttempt() {
        return attempt;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public long getFencingToken() {
        return fencingToken;
    }

    public void assignLease(String workerId, Instant expiresAt) {
        this.workerId = workerId;
        this.leaseExpiresAt = expiresAt;
        status.set(ExecutionStatus.LEASED);
    }

    public void markRunning() {
        this.startedAt = Instant.now();
        status.set(ExecutionStatus.RUNNING);
    }

    public void markSucceeded() {
        this.finishedAt = Instant.now();
        status.set(ExecutionStatus.SUCCEEDED);
    }

    public void markFailed(String error) {
        this.errorMessage = error;
        this.finishedAt = Instant.now();
        status.set(ExecutionStatus.FAILED);
    }

    public void markDeadLettered(String error) {
        this.errorMessage = error;
        this.finishedAt = Instant.now();
        status.set(ExecutionStatus.DEAD_LETTERED);
    }

    public void markSkipped(String reason) {
        this.errorMessage = reason;
        this.finishedAt = Instant.now();
        status.set(ExecutionStatus.SKIPPED);
    }

    public void bumpAttempt() {
        this.attempt++;
    }

    public boolean isTerminal() {
        ExecutionStatus s = status.get();
        return s == ExecutionStatus.SUCCEEDED
                || s == ExecutionStatus.DEAD_LETTERED
                || s == ExecutionStatus.SKIPPED;
    }
}
