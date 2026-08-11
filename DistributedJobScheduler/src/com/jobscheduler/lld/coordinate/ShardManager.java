package com.jobscheduler.lld.coordinate;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Assigns job shards to healthy scheduler/worker nodes via consistent hashing.
 * Reassigns when heartbeats go silent.
 */
public final class ShardManager {
    public static final class WorkerRegistration {
        private final String workerId;
        private volatile Instant lastHeartbeat;
        private volatile boolean alive = true;

        WorkerRegistration(String workerId, Instant now) {
            this.workerId = workerId;
            this.lastHeartbeat = now;
        }

        public String getWorkerId() {
            return workerId;
        }

        public Instant getLastHeartbeat() {
            return lastHeartbeat;
        }

        public boolean isAlive() {
            return alive;
        }

        void heartbeat(Instant now) {
            this.lastHeartbeat = now;
            this.alive = true;
        }

        void markDead() {
            this.alive = false;
        }
    }

    private final int shardCount;
    private final ConsistentHashRing ring;
    private final Map<String, WorkerRegistration> workers = new ConcurrentHashMap<>();
    private final Map<Integer, String> shardOwners = new ConcurrentHashMap<>();
    private final List<String> rebalanceLog = new CopyOnWriteArrayList<>();

    public ShardManager(int shardCount, int virtualNodes) {
        this.shardCount = shardCount;
        this.ring = new ConsistentHashRing(virtualNodes);
    }

    public int getShardCount() {
        return shardCount;
    }

    public void registerWorker(String workerId, Instant now) {
        workers.put(workerId, new WorkerRegistration(workerId, now));
        ring.addWorker(workerId);
        rebalance(now);
    }

    public void heartbeat(String workerId, Instant now) {
        WorkerRegistration reg = workers.get(workerId);
        if (reg != null) {
            reg.heartbeat(now);
        }
    }

    public void deregisterWorker(String workerId, Instant now) {
        workers.remove(workerId);
        ring.removeWorker(workerId);
        rebalance(now);
    }

    /**
     * Mark workers silent longer than {@code timeout} as dead and rebalance.
     */
    public Set<String> evictSilent(Instant now, java.time.Duration timeout) {
        Set<String> evicted = ConcurrentHashMap.newKeySet();
        for (WorkerRegistration reg : workers.values()) {
            if (reg.lastHeartbeat.plus(timeout).isBefore(now)) {
                reg.markDead();
                ring.removeWorker(reg.workerId);
                workers.remove(reg.workerId);
                evicted.add(reg.workerId);
            }
        }
        if (!evicted.isEmpty()) {
            rebalance(now);
        }
        return evicted;
    }

    public void rebalance(Instant now) {
        shardOwners.clear();
        List<String> alive = ring.workers();
        if (alive.isEmpty()) {
            rebalanceLog.add(now + " rebalance: no workers");
            return;
        }
        // Consistent-hash primary owner, with round-robin fallback when the ring
        // collapses onto one node (common with tiny shard counts in demos).
        Map<String, Integer> load = new ConcurrentHashMap<>();
        for (String w : alive) {
            load.put(w, 0);
        }
        for (int shard = 0; shard < shardCount; shard++) {
            String owner = ring.ownerOf("shard-" + shard);
            if (owner == null) {
                owner = alive.get(shard % alive.size());
            }
            // Soft rebalance: if one node owns too many, hand to least-loaded
            int maxFair = (shardCount + alive.size() - 1) / alive.size();
            if (load.getOrDefault(owner, 0) >= maxFair && alive.size() > 1) {
                owner = alive.stream()
                        .min(Comparator.comparingInt(w -> load.getOrDefault(w, 0)))
                        .orElse(owner);
            }
            load.merge(owner, 1, Integer::sum);
            shardOwners.put(shard, owner);
        }
        rebalanceLog.add(now + " rebalance: " + shardOwners);
    }

    public String ownerOfShard(int shardId) {
        return shardOwners.get(shardId);
    }

    public List<Integer> shardsOwnedBy(String workerId) {
        List<Integer> owned = new ArrayList<>();
        for (Map.Entry<Integer, String> e : shardOwners.entrySet()) {
            if (workerId.equals(e.getValue())) {
                owned.add(e.getKey());
            }
        }
        Collections.sort(owned);
        return owned;
    }

    public Map<Integer, String> snapshotOwners() {
        return Map.copyOf(shardOwners);
    }

    public List<WorkerRegistration> listWorkers() {
        return new ArrayList<>(workers.values());
    }

    public List<String> getRebalanceLog() {
        return List.copyOf(rebalanceLog);
    }
}
