package com.jobscheduler.lld.store;

import com.jobscheduler.lld.job.JobExecution;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Dead letter queue for executions that exhausted retries.
 * <p>
 * Why: never retry forever — poison jobs must be quarantined for ops review.
 */
public final class DeadLetterQueue {
    public static final class DeadLetter {
        private final JobExecution execution;
        private final String reason;
        private final Instant deadLetteredAt;

        public DeadLetter(JobExecution execution, String reason) {
            this.execution = execution;
            this.reason = reason;
            this.deadLetteredAt = Instant.now();
        }

        public JobExecution getExecution() {
            return execution;
        }

        public String getReason() {
            return reason;
        }

        public Instant getDeadLetteredAt() {
            return deadLetteredAt;
        }
    }

    private final Map<String, DeadLetter> entries = new ConcurrentHashMap<>();

    public void enqueue(JobExecution execution, String reason) {
        execution.markDeadLettered(reason);
        entries.put(execution.getExecutionId(), new DeadLetter(execution, reason));
    }

    public List<DeadLetter> list() {
        return new ArrayList<>(entries.values());
    }

    public int size() {
        return entries.size();
    }

    public boolean remove(String executionId) {
        return entries.remove(executionId) != null;
    }
}
