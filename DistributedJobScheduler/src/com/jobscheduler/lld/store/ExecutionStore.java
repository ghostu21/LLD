package com.jobscheduler.lld.store;

import com.jobscheduler.lld.job.ExecutionStatus;
import com.jobscheduler.lld.job.JobExecution;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Execution log + idempotency store.
 * <p>
 * Unique on idempotencyKey so retries / redeliveries cannot double-execute.
 */
public final class ExecutionStore {
    private final Map<String, JobExecution> byId = new ConcurrentHashMap<>();
    private final Map<String, JobExecution> byIdempotencyKey = new ConcurrentHashMap<>();

    /**
     * Insert if absent. Returns existing row when the key was already seen
     * (duplicate delivery → skip work).
     */
    public JobExecution putIfAbsent(JobExecution execution) {
        JobExecution existing = byIdempotencyKey.putIfAbsent(
                execution.getIdempotencyKey(), execution);
        if (existing != null) {
            return existing;
        }
        byId.put(execution.getExecutionId(), execution);
        return execution;
    }

    public Optional<JobExecution> findById(String executionId) {
        return Optional.ofNullable(byId.get(executionId));
    }

    public Optional<JobExecution> findByIdempotencyKey(String key) {
        return Optional.ofNullable(byIdempotencyKey.get(key));
    }

    public List<JobExecution> findByJobId(String jobId) {
        return byId.values().stream()
                .filter(e -> e.getJobId().equals(jobId))
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<JobExecution> findByStatus(ExecutionStatus status) {
        return byId.values().stream()
                .filter(e -> e.getStatus() == status)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public boolean hasActiveRun(String jobId) {
        return byId.values().stream()
                .anyMatch(e -> e.getJobId().equals(jobId)
                        && (e.getStatus() == ExecutionStatus.LEASED
                        || e.getStatus() == ExecutionStatus.RUNNING));
    }

    public int size() {
        return byId.size();
    }
}
