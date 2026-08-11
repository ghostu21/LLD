package com.jobscheduler.lld.worker;

import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.JobExecution;
import com.jobscheduler.lld.job.RetryPolicy;
import com.jobscheduler.lld.schedule.DueScanner;
import com.jobscheduler.lld.store.DeadLetterQueue;
import com.jobscheduler.lld.store.ExecutionStore;
import com.jobscheduler.lld.store.JobStore;

import java.time.Duration;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
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
    private final BiConsumer<Job, JobExecution> handler;

    public JobExecutor(String workerId, JobStore jobStore, ExecutionStore executionStore,
                       DeadLetterQueue deadLetterQueue, Duration leaseTtl,
                       AtomicLong acceptedFencingToken,
                       BiConsumer<Job, JobExecution> handler) {
        this.workerId = Objects.requireNonNull(workerId);
        this.jobStore = jobStore;
        this.executionStore = executionStore;
        this.deadLetterQueue = deadLetterQueue;
        this.leaseTtl = leaseTtl != null ? leaseTtl : Duration.ofSeconds(30);
        this.acceptedFencingToken = acceptedFencingToken;
        this.handler = handler != null ? handler : (j, e) -> { /* no-op success */ };
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
            // Duplicate delivery — already recorded
            if (execution.isTerminal()) {
                return execution;
            }
            // Retry path: bump attempt under new lease if previous failed
            if (execution.getStatus().name().equals("FAILED")) {
                return retry(job, execution, now, fencingToken);
            }
            return execution;
        }

        return run(job, execution, now);
    }

    private JobExecution retry(Job job, JobExecution execution, Instant now, long fencingToken) {
        RetryPolicy retry = job.getRetryPolicy();
        if (execution.getAttempt() >= retry.getMaxAttempts()) {
            deadLetterQueue.enqueue(execution, "max attempts exhausted");
            return execution;
        }
        execution.bumpAttempt();
        execution.assignLease(workerId, now.plus(leaseTtl));
        return run(job, execution, now);
    }

    private JobExecution run(Job job, JobExecution execution, Instant now) {
        execution.markRunning();
        job.setLastIdempotencyKey(execution.getIdempotencyKey());
        try {
            handler.accept(job, execution);
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
     * Reclaim expired leases (worker crash recovery).
     */
    public void reclaimExpiredLeases(Instant now) {
        executionStore.findByStatus(com.jobscheduler.lld.job.ExecutionStatus.LEASED).stream()
                .filter(e -> e.getLeaseExpiresAt() != null && e.getLeaseExpiresAt().isBefore(now))
                .forEach(e -> {
                    Job job = jobStore.findById(e.getJobId()).orElse(null);
                    if (job == null) {
                        return;
                    }
                    e.markFailed("lease expired");
                    if (e.getAttempt() >= job.getRetryPolicy().getMaxAttempts()) {
                        deadLetterQueue.enqueue(e, "lease expired, max attempts");
                    }
                });
        executionStore.findByStatus(com.jobscheduler.lld.job.ExecutionStatus.RUNNING).stream()
                .filter(e -> e.getLeaseExpiresAt() != null && e.getLeaseExpiresAt().isBefore(now))
                .forEach(e -> {
                    Job job = jobStore.findById(e.getJobId()).orElse(null);
                    if (job == null) {
                        return;
                    }
                    e.markFailed("lease expired while running");
                    if (e.getAttempt() >= job.getRetryPolicy().getMaxAttempts()) {
                        deadLetterQueue.enqueue(e, "lease expired while running");
                    }
                });
    }
}
