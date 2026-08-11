package com.jobscheduler.lld.api;

import com.jobscheduler.lld.clock.HybridClock;
import com.jobscheduler.lld.job.CatchUpPolicy;
import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.JobExecution;
import com.jobscheduler.lld.job.JobStatus;
import com.jobscheduler.lld.job.OverlapPolicy;
import com.jobscheduler.lld.job.RetryPolicy;
import com.jobscheduler.lld.job.ScheduleSpec;
import com.jobscheduler.lld.monitor.MonitoringDashboard;
import com.jobscheduler.lld.schedule.CronExpression;
import com.jobscheduler.lld.store.ExecutionStore;
import com.jobscheduler.lld.store.JobStore;
import com.jobscheduler.lld.worker.WorkerPool;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Public scheduler APIs:
 * {@code scheduleJob}, {@code cancelJob}, {@code getJobStatus},
 * plus pause/resume/update from production cron systems (Cadence Schedules).
 */
public final class JobSchedulerApi {
    public static final class JobStatusView {
        private final String jobId;
        private final JobStatus status;
        private final Instant nextRunAt;
        private final Instant lastRunAt;
        private final String lastIdempotencyKey;
        private final List<JobExecution> recentExecutions;

        public JobStatusView(String jobId, JobStatus status, Instant nextRunAt,
                             Instant lastRunAt, String lastIdempotencyKey,
                             List<JobExecution> recentExecutions) {
            this.jobId = jobId;
            this.status = status;
            this.nextRunAt = nextRunAt;
            this.lastRunAt = lastRunAt;
            this.lastIdempotencyKey = lastIdempotencyKey;
            this.recentExecutions = recentExecutions;
        }

        public String getJobId() {
            return jobId;
        }

        public JobStatus getStatus() {
            return status;
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

        public List<JobExecution> getRecentExecutions() {
            return recentExecutions;
        }

        @Override
        public String toString() {
            return "JobStatus{id=" + jobId
                    + ", status=" + status
                    + ", next=" + nextRunAt
                    + ", last=" + lastRunAt
                    + ", idem=" + lastIdempotencyKey
                    + ", runs=" + recentExecutions.size() + "}";
        }
    }

    private final JobStore jobStore;
    private final ExecutionStore executionStore;
    private final HybridClock clock;
    private final WorkerPool workerPool;
    private final MonitoringDashboard dashboard;

    public JobSchedulerApi(JobStore jobStore, ExecutionStore executionStore,
                           HybridClock clock, WorkerPool workerPool,
                           MonitoringDashboard dashboard) {
        this.jobStore = jobStore;
        this.executionStore = executionStore;
        this.clock = clock;
        this.workerPool = workerPool;
        this.dashboard = dashboard;
    }

    /**
     * scheduleJob(jobId, payload, cronExpr | runAt)
     */
    public Job scheduleJob(String jobId, String payload, ScheduleSpec schedule) {
        return scheduleJob(jobId, "default", payload, schedule,
                OverlapPolicy.SKIP, CatchUpPolicy.SKIP, RetryPolicy.defaults(), 0);
    }

    public Job scheduleJob(String jobId, String tenantId, String payload, ScheduleSpec schedule,
                           OverlapPolicy overlap, CatchUpPolicy catchUp,
                           RetryPolicy retry, int priority) {
        Objects.requireNonNull(jobId, "jobId");
        Objects.requireNonNull(schedule, "schedule");
        if (jobStore.findById(jobId).isPresent()) {
            throw new IllegalArgumentException("job already exists: " + jobId);
        }

        Instant next = computeFirstFire(schedule, clock.now());
        Job job = new Job(jobId, tenantId, payload, schedule, overlap, catchUp,
                retry, priority, jobId, next);
        jobStore.save(job);
        workerPool.refreshAllScanners();
        return job;
    }

    public boolean cancelJob(String jobId) {
        Optional<Job> opt = jobStore.findById(jobId);
        if (opt.isEmpty()) {
            return false;
        }
        Job job = opt.get();
        job.forceStatus(JobStatus.CANCELLED);
        return true;
    }

    public boolean pauseJob(String jobId) {
        return jobStore.findById(jobId)
                .map(j -> j.transitionStatus(JobStatus.ACTIVE, JobStatus.PAUSED)
                        || j.getStatus() == JobStatus.PAUSED)
                .orElse(false);
    }

    public boolean resumeJob(String jobId) {
        Optional<Job> opt = jobStore.findById(jobId);
        if (opt.isEmpty()) {
            return false;
        }
        Job job = opt.get();
        if (job.getStatus() != JobStatus.PAUSED) {
            return false;
        }
        // Recompute next from now so we don't stampede catch-up unless policy says so
        Instant next = computeFirstFire(job.getSchedule(), clock.now());
        job.setNextRunAt(next);
        job.forceStatus(JobStatus.ACTIVE);
        workerPool.refreshAllScanners();
        return true;
    }

    public JobStatusView getJobStatus(String jobId) {
        Job job = jobStore.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("unknown job: " + jobId));
        return new JobStatusView(
                job.getJobId(),
                job.getStatus(),
                job.getNextRunAt(),
                job.getLastRunAt(),
                job.getLastIdempotencyKey(),
                executionStore.findByJobId(jobId));
    }

    public MonitoringDashboard.Snapshot getDashboard() {
        return dashboard.snapshot();
    }

    public String renderDashboard() {
        return dashboard.renderText();
    }

    private Instant computeFirstFire(ScheduleSpec schedule, Instant now) {
        if (schedule.getType() == com.jobscheduler.lld.job.JobType.ONE_OFF) {
            Instant runAt = schedule.getRunAt().orElseThrow();
            if (runAt.isBefore(now)) {
                throw new IllegalArgumentException("runAt is in the past: " + runAt);
            }
            return runAt;
        }
        String cron = schedule.getCronExpr().orElseThrow();
        ZoneId zone = schedule.getTimezone();
        return new CronExpression(cron).nextAfter(now.minusSeconds(1), zone);
    }
}
