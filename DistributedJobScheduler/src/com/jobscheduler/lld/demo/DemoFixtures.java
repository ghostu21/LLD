package com.jobscheduler.lld.demo;

import com.jobscheduler.lld.api.JobSchedulerApi;
import com.jobscheduler.lld.clock.HybridClock;
import com.jobscheduler.lld.coordinate.Coordinator;
import com.jobscheduler.lld.monitor.MonitoringDashboard;
import com.jobscheduler.lld.store.DeadLetterQueue;
import com.jobscheduler.lld.store.ExecutionStore;
import com.jobscheduler.lld.store.JobStore;
import com.jobscheduler.lld.worker.WorkerPool;

import java.time.Duration;
import java.time.Instant;

/**
 * Shared fixtures wiring Coordinator → ShardManager → WorkerPool → JobStore.
 */
public final class DemoFixtures {
    public final HybridClock clock = HybridClock.defaults();
    public final JobStore jobStore = new JobStore();
    public final ExecutionStore executionStore = new ExecutionStore();
    public final DeadLetterQueue deadLetterQueue = new DeadLetterQueue();
    public final MonitoringDashboard dashboard;
    public final Coordinator coordinator;
    public final WorkerPool workerPool;
    public final JobSchedulerApi api;

    public DemoFixtures() {
        this(4);
    }

    public DemoFixtures(int shardCount) {
        dashboard = new MonitoringDashboard(
                jobStore, executionStore, deadLetterQueue, Duration.ofSeconds(2));
        coordinator = new Coordinator(
                "coordinator-1", shardCount, Duration.ofSeconds(15), Duration.ofSeconds(10));
        Instant now = clock.now();
        if (!coordinator.tryBecomeLeader(now)) {
            throw new IllegalStateException("failed to elect coordinator");
        }
        workerPool = new WorkerPool(
                jobStore, executionStore, deadLetterQueue, coordinator, clock, dashboard);
        workerPool.syncFencingToken(coordinator.getActiveFencingToken());
        api = new JobSchedulerApi(jobStore, executionStore, clock, workerPool, dashboard);
    }

    public void bootstrapWorkers(String... workerIds) {
        Instant now = clock.now();
        for (String id : workerIds) {
            workerPool.addWorker(id, now);
        }
        workerPool.refreshAllScanners();
    }
}
