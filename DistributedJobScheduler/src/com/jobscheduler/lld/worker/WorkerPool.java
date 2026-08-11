package com.jobscheduler.lld.worker;

import com.jobscheduler.lld.clock.HybridClock;
import com.jobscheduler.lld.coordinate.Coordinator;
import com.jobscheduler.lld.job.JobExecution;
import com.jobscheduler.lld.monitor.MonitoringDashboard;
import com.jobscheduler.lld.schedule.DueScanner;
import com.jobscheduler.lld.store.DeadLetterQueue;
import com.jobscheduler.lld.store.ExecutionStore;
import com.jobscheduler.lld.store.JobStore;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;

/**
 * Worker pool: each worker owns shards assigned by the coordinator,
 * runs a due-scanner tick, and executes leased jobs.
 */
public final class WorkerPool {
    public static final class WorkerNode {
        private final String workerId;
        private final JobExecutor executor;
        private final Map<Integer, DueScanner> scanners = new ConcurrentHashMap<>();

        WorkerNode(String workerId, JobExecutor executor) {
            this.workerId = workerId;
            this.executor = executor;
        }

        public String getWorkerId() {
            return workerId;
        }

        public JobExecutor getExecutor() {
            return executor;
        }

        public Map<Integer, DueScanner> getScanners() {
            return scanners;
        }
    }

    private final JobStore jobStore;
    private final ExecutionStore executionStore;
    private final DeadLetterQueue deadLetterQueue;
    private final Coordinator coordinator;
    private final HybridClock clock;
    private final MonitoringDashboard dashboard;
    private final AtomicLong fencingToken = new AtomicLong(0);
    private final Map<String, WorkerNode> workers = new ConcurrentHashMap<>();
    private final List<JobExecution> recentExecutions = new CopyOnWriteArrayList<>();
    private final AtomicReference<BiConsumer<com.jobscheduler.lld.job.Job, JobExecution>> jobHandler =
            new AtomicReference<>((j, e) -> { /* default success */ });

    public WorkerPool(JobStore jobStore, ExecutionStore executionStore,
                      DeadLetterQueue deadLetterQueue, Coordinator coordinator,
                      HybridClock clock, MonitoringDashboard dashboard) {
        this.jobStore = jobStore;
        this.executionStore = executionStore;
        this.deadLetterQueue = deadLetterQueue;
        this.coordinator = coordinator;
        this.clock = clock;
        this.dashboard = dashboard;
    }

    public void setJobHandler(BiConsumer<com.jobscheduler.lld.job.Job, JobExecution> handler) {
        this.jobHandler.set(handler != null ? handler : (j, e) -> { });
    }

    public void syncFencingToken(long token) {
        fencingToken.set(token);
    }

    public WorkerNode addWorker(String workerId, Instant now) {
        JobExecutor executor = new JobExecutor(
                workerId, jobStore, executionStore, deadLetterQueue,
                Duration.ofSeconds(30), fencingToken, jobHandler);
        WorkerNode node = new WorkerNode(workerId, executor);
        workers.put(workerId, node);
        if (coordinator.isLeader(now)) {
            coordinator.registerWorker(workerId, now);
        }
        refreshScanners(workerId);
        return node;
    }

    public void heartbeat(String workerId, Instant now) {
        coordinator.workerHeartbeat(workerId, now);
    }

    public void removeWorker(String workerId, Instant now) {
        workers.remove(workerId);
        if (coordinator.isLeader(now)) {
            coordinator.getShardManager().deregisterWorker(workerId, now);
        }
    }

    public void refreshScanners(String workerId) {
        WorkerNode node = workers.get(workerId);
        if (node == null) {
            return;
        }
        node.scanners.clear();
        Instant now = clock.now();
        for (int shard : coordinator.shardsFor(workerId)) {
            DueScanner scanner = new DueScanner(
                    shard, coordinator.getShardManager().getShardCount(),
                    jobStore, executionStore, clock,
                    Duration.ofHours(1), Duration.ofMillis(100));
            scanner.reloadWheel(now);
            node.scanners.put(shard, scanner);
        }
    }

    public void refreshAllScanners() {
        for (String id : workers.keySet()) {
            refreshScanners(id);
        }
    }

    /**
     * One scheduling + execution tick across all workers.
     */
    public List<JobExecution> tick(Instant now) {
        List<JobExecution> done = new ArrayList<>();
        long token = fencingToken.get();
        for (WorkerNode node : workers.values()) {
            heartbeat(node.workerId, now);
            node.executor.reclaimExpiredLeases(now);
            for (DueScanner scanner : node.scanners.values()) {
                for (DueScanner.FireIntent intent : scanner.tick(now)) {
                    JobExecution exec = node.executor.execute(intent, token, now);
                    done.add(exec);
                    recentExecutions.add(exec);
                    dashboard.recordExecution(exec, clock);
                }
            }
        }
        return done;
    }

    public List<WorkerNode> listWorkers() {
        return new ArrayList<>(workers.values());
    }

    public List<JobExecution> getRecentExecutions() {
        return List.copyOf(recentExecutions);
    }
}
