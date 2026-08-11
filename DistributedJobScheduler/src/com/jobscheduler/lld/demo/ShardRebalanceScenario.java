package com.jobscheduler.lld.demo;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/**
 * Coordinator leader election + shard rebalance on worker join/leave.
 */
public final class ShardRebalanceScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Shard rebalance / leader ---");
        Instant now = fx.clock.now();
        System.out.println("leader=" + fx.coordinator.isLeader(now)
                + " fencingToken=" + fx.coordinator.getActiveFencingToken());

        fx.bootstrapWorkers("w1", "w2");
        Map<Integer, String> owners1 = fx.coordinator.getShardManager().snapshotOwners();
        System.out.println("owners with 2 workers: " + owners1);

        fx.workerPool.removeWorker("w2", fx.clock.now());
        Map<Integer, String> owners2 = fx.coordinator.getShardManager().snapshotOwners();
        System.out.println("owners after w2 leave: " + owners2);

        // Register a worker that will go silent; keep w1 healthy via heartbeat
        fx.bootstrapWorkers("silent-worker");
        Instant later = fx.clock.now().plusSeconds(12);
        fx.coordinator.renewLeadership(later);
        fx.coordinator.workerHeartbeat("w1", later);
        Set<String> evicted = fx.coordinator.checkHealth(later);
        System.out.println("evicted silent workers=" + evicted
                + " owners=" + fx.coordinator.getShardManager().snapshotOwners());
    }
}
