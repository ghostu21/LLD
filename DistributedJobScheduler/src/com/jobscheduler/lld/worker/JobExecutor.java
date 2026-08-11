package com.jobscheduler.lld.worker;

import com.jobscheduler.lld.job.ExecutionStatus;
import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.JobExecution;
import com.jobscheduler.lld.job.RetryPolicy;
import com.jobscheduler.lld.schedule.DueScanner;
import com.jobscheduler.lld.store.DeadLetterQueue;
import com.jobscheduler.lld.store.ExecutionStore;
import com.jobscheduler.lld.store.JobStore;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * Pulls fire intents via lease — crashed workers lose the lease, not the job.
 * Idempotency keys prevent double-execution on retry.
 */
public final class JobExecutor {
    private final String workerId;
    private final JobStore jobStore;
    private final ExecutionStore executionStore;
    private final DeadLetterQueue deadLetterQueue;
    private final Duration leaseTtl;
    private final AtomicLong acceptedFencingToken;
    private final AtomicReference<BiConsumer<Job, JobExecution>> handler;

    public JobExecutor(String workerId, JobStore jobStore, ExecutionStore executionStore,
                       DeadLetterQueue deadLetterQueue, Duration leaseTtl,
                       AtomicLong acceptedFencingToken,
                       AtomicReference<BiConsumer<Job, JobExecution>> handler) {
        this.workerId = Objects.requireNonNull(workerId);
        this.jobStore = jobStore;
        this.executionStore = executionStore;
        this.deadLetterQueue = deadLetterQueue;
        this.leaseTtl = leaseTtl != null ? leaseTtl : Duration.ofSeconds(30);
        this.acceptedFencingToken = acceptedFencingToken;
        this.handler = handler;
    }

    public String getWorkerId() {
        return workerId;
    }

    /**
     * Claim + execute one fire. Returns the execution row (existing if duplicate).
     */
    public JobExecution execute(DueScanner.FireIntent intent, long fencingToken, Instant now) {
        if (fencingToken < acceptedFencingToken.get()) {
            throw new IllegalStateException("stale fencing token " + fencingToken);
        }
        acceptedFencingToken.updateAndGet(v -> Math.max(v, fencingToken));

        Job job = jobStore.findById(intent.getJobId())
                .orElseThrow(() -> new IllegalArgumentException("unknown job " + intent.getJobId()));

        String idem = Job.idempotencyKey(job.getJobId(), intent.getScheduledFireAt());
        JobExecution candidate = new JobExecution(
                UUID.randomUUID().toString(),
                job.getJobId(),
                idem,
                intent.getScheduledFireAt(),
                1,
                fencingToken);
        candidate.assignLease(workerId, now.plus(leaseTtl));

        JobExecution execution = executionStore.putIfAbsent(candidate);
        if (execution != candidate) {
            if (execution.isTerminal()) {
                return execution;
            }
            if (execution.getStatus() == ExecutionStatus.FAILED) {
                return retry(job, execution, now);
            }
            // Still LEASED/RUNNING on another worker — do not steal unless lease expired
            if (execution.getLeaseExpiresAt() != null && execution.getLeaseExpiresAt().isBefore(now)) {
                return retry(job, execution, now);
            }
            return execution;
        }

        return run(job, execution);
    }

    private JobExecution retry(Job job, JobExecution execution, Instant now) {
        RetryPolicy retry = job.getRetryPolicy();
        if (execution.getAttempt() >= retry.getMaxAttempts()) {
            deadLetterQueue.enqueue(execution, "max attempts exhausted");
            return execution;
        }
        execution.bumpAttempt();
        execution.assignLease(workerId, now.plus(leaseTtl));
        return run(job, execution);
    }

    private JobExecution run(Job job, JobExecution execution) {
        execution.markRunning();
        job.setLastIdempotencyKey(execution.getIdempotencyKey());
        try {
            BiConsumer<Job, JobExecution> h = handler.get();
            if (h != null) {
                h.accept(job, execution);
            }
            execution.markSucceeded();
        } catch (RuntimeException ex) {
            execution.markFailed(ex.getMessage());
            RetryPolicy retry = job.getRetryPolicy();
            if (execution.getAttempt() >= retry.getMaxAttempts()) {
                deadLetterQueue.enqueue(execution, ex.getMessage());
            }
        }
        return execution;
    }

    /**
     * Reclaim expired leases (worker crash recovery) — mark failed and leave
     * eligible for retry on the next delivery of the same fire / explicit retry.
     */
    public void reclaimExpiredLeases(Instant now) {
        for (ExecutionStatus status : List.of(ExecutionStatus.LEASED, ExecutionStatus.RUNNING)) {
            for (JobExecution e : executionStore.findByStatus(status)) {
                if (e.getLeaseExpiresAt() == null || !e.getLeaseExpiresAt().isBefore(now)) {
                    continue;
                }
                Job job = jobStore.findById(e.getJobId()).orElse(null);
                if (job == null) {
                    continue;
                }
                String reason = status == ExecutionStatus.RUNNING
                        ? "lease expired while running"
                        : "lease expired";
                e.markFailed(reason);
                if (e.getAttempt() >= job.getRetryPolicy().getMaxAttempts()) {
                    deadLetterQueue.enqueue(e, reason + ", max attempts");
                }
            }
        }
    }
}
