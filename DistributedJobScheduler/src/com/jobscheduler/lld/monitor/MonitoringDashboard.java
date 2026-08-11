package com.jobscheduler.lld.monitor;

import com.jobscheduler.lld.clock.HybridClock;
import com.jobscheduler.lld.job.ExecutionStatus;
import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.JobExecution;
import com.jobscheduler.lld.job.JobStatus;
import com.jobscheduler.lld.store.DeadLetterQueue;
import com.jobscheduler.lld.store.ExecutionStore;
import com.jobscheduler.lld.store.JobStore;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * Bonus: monitoring for missed executions and scheduling drift.
 * <p>
 * In production this feeds Prometheus / a UI dashboard. Here it is an
 * in-process snapshot API suitable for LLD demos and interview walkthroughs.
 */
public final class MonitoringDashboard {
    public static final class DriftSample {
        private final String jobId;
        private final String executionId;
        private final Instant scheduledFireAt;
        private final Instant actualFireAt;
        private final Duration drift;
        private final boolean missed;

        public DriftSample(String jobId, String executionId, Instant scheduledFireAt,
                           Instant actualFireAt, Duration drift, boolean missed) {
            this.jobId = jobId;
            this.executionId = executionId;
            this.scheduledFireAt = scheduledFireAt;
            this.actualFireAt = actualFireAt;
            this.drift = drift;
            this.missed = missed;
        }

        public String getJobId() {
            return jobId;
        }

        public String getExecutionId() {
            return executionId;
        }

        public Instant getScheduledFireAt() {
            return scheduledFireAt;
        }

        public Instant getActualFireAt() {
            return actualFireAt;
        }

        public Duration getDrift() {
            return drift;
        }

        public boolean isMissed() {
            return missed;
        }
    }

    public static final class Snapshot {
        private final int activeJobs;
        private final int pausedJobs;
        private final long totalExecutions;
        private final long succeeded;
        private final long failed;
        private final long deadLettered;
        private final long missed;
        private final Duration p99Drift;
        private final Duration maxDrift;
        private final List<DriftSample> recentMisses;
        private final List<DriftSample> topDrift;

        Snapshot(int activeJobs, int pausedJobs, long totalExecutions, long succeeded,
                 long failed, long deadLettered, long missed, Duration p99Drift,
                 Duration maxDrift, List<DriftSample> recentMisses, List<DriftSample> topDrift) {
            this.activeJobs = activeJobs;
            this.pausedJobs = pausedJobs;
            this.totalExecutions = totalExecutions;
            this.succeeded = succeeded;
            this.failed = failed;
            this.deadLettered = deadLettered;
            this.missed = missed;
            this.p99Drift = p99Drift;
            this.maxDrift = maxDrift;
            this.recentMisses = recentMisses;
            this.topDrift = topDrift;
        }

        public int getActiveJobs() {
            return activeJobs;
        }

        public int getPausedJobs() {
            return pausedJobs;
        }

        public long getTotalExecutions() {
            return totalExecutions;
        }

        public long getSucceeded() {
            return succeeded;
        }

        public long getFailed() {
            return failed;
        }

        public long getDeadLettered() {
            return deadLettered;
        }

        public long getMissed() {
            return missed;
        }

        public Duration getP99Drift() {
            return p99Drift;
        }

        public Duration getMaxDrift() {
            return maxDrift;
        }

        public List<DriftSample> getRecentMisses() {
            return recentMisses;
        }

        public List<DriftSample> getTopDrift() {
            return topDrift;
        }

        @Override
        public String toString() {
            return "Dashboard{active=" + activeJobs
                    + ", paused=" + pausedJobs
                    + ", exec=" + totalExecutions
                    + ", ok=" + succeeded
                    + ", fail=" + failed
                    + ", dlq=" + deadLettered
                    + ", missed=" + missed
                    + ", p99Drift=" + p99Drift.toMillis() + "ms"
                    + ", maxDrift=" + maxDrift.toMillis() + "ms}";
        }
    }

    private final JobStore jobStore;
    private final ExecutionStore executionStore;
    private final DeadLetterQueue deadLetterQueue;
    private final Duration missThreshold;
    private final List<DriftSample> samples = new CopyOnWriteArrayList<>();
    private final AtomicLong missedCount = new AtomicLong();
    private final Map<ExecutionStatus, AtomicLong> statusCounts = new ConcurrentHashMap<>();

    public MonitoringDashboard(JobStore jobStore, ExecutionStore executionStore,
                               DeadLetterQueue deadLetterQueue, Duration missThreshold) {
        this.jobStore = jobStore;
        this.executionStore = executionStore;
        this.deadLetterQueue = deadLetterQueue;
        this.missThreshold = missThreshold != null ? missThreshold : Duration.ofSeconds(5);
        for (ExecutionStatus s : ExecutionStatus.values()) {
            statusCounts.put(s, new AtomicLong());
        }
    }

    public void recordExecution(JobExecution execution, HybridClock clock) {
        Instant actual = execution.getStartedAt() != null
                ? execution.getStartedAt()
                : Instant.now();
        Duration drift = clock.lateness(execution.getScheduledFireAt(), actual);
        boolean missed = drift.compareTo(missThreshold) > 0;
        if (missed) {
            missedCount.incrementAndGet();
        }
        statusCounts.get(execution.getStatus()).incrementAndGet();
        samples.add(new DriftSample(
                execution.getJobId(),
                execution.getExecutionId(),
                execution.getScheduledFireAt(),
                actual,
                drift,
                missed));
        if (samples.size() > 5000) {
            samples.subList(0, samples.size() - 4000).clear();
        }
    }

    public Snapshot snapshot() {
        int active = 0;
        int paused = 0;
        for (Job j : jobStore.findAll()) {
            if (j.getStatus() == JobStatus.ACTIVE) {
                active++;
            } else if (j.getStatus() == JobStatus.PAUSED) {
                paused++;
            }
        }

        List<DriftSample> drifts = samples.stream()
                .sorted(Comparator.comparing(DriftSample::getDrift).reversed())
                .limit(10)
                .collect(Collectors.toCollection(ArrayList::new));

        List<DriftSample> misses = samples.stream()
                .filter(DriftSample::isMissed)
                .sorted(Comparator.comparing(DriftSample::getActualFireAt).reversed())
                .limit(10)
                .collect(Collectors.toCollection(ArrayList::new));

        Duration p99 = percentile(0.99);
        Duration max = samples.stream()
                .map(DriftSample::getDrift)
                .max(Duration::compareTo)
                .orElse(Duration.ZERO);

        return new Snapshot(
                active,
                paused,
                samples.size(),
                statusCounts.get(ExecutionStatus.SUCCEEDED).get(),
                statusCounts.get(ExecutionStatus.FAILED).get(),
                deadLetterQueue.size(),
                missedCount.get(),
                p99,
                max,
                misses,
                drifts);
    }

    public String renderText() {
        Snapshot s = snapshot();
        StringBuilder sb = new StringBuilder();
        sb.append("=== Job Scheduler Monitoring Dashboard ===\n");
        sb.append(s).append('\n');
        sb.append("--- Recent missed executions (drift > ")
                .append(missThreshold.toMillis()).append("ms) ---\n");
        for (DriftSample m : s.getRecentMisses()) {
            sb.append(String.format("  job=%s drift=%dms scheduled=%s actual=%s%n",
                    m.getJobId(), m.getDrift().toMillis(),
                    m.getScheduledFireAt(), m.getActualFireAt()));
        }
        sb.append("--- Top scheduling drift ---\n");
        for (DriftSample d : s.getTopDrift()) {
            sb.append(String.format("  job=%s drift=%dms%n",
                    d.getJobId(), d.getDrift().toMillis()));
        }
        sb.append("--- DLQ size: ").append(deadLetterQueue.size()).append(" ---\n");
        return sb.toString();
    }

    private Duration percentile(double p) {
        if (samples.isEmpty()) {
            return Duration.ZERO;
        }
        List<Long> millis = samples.stream()
                .map(s -> s.getDrift().toMillis())
                .sorted()
                .collect(Collectors.toList());
        int idx = Math.min(millis.size() - 1, (int) Math.ceil(p * millis.size()) - 1);
        return Duration.ofMillis(millis.get(Math.max(0, idx)));
    }
}
