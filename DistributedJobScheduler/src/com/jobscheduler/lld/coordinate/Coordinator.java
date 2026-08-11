package com.jobscheduler.lld.coordinate;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Cluster coordinator: leader election + shard assignment + health checks.
 * <p>
 * Architecture: Coordinator → Shard Manager → Worker Pool → Job Store
 */
public final class Coordinator {
    private final String nodeId;
    private final LeaderElection election;
    private final ShardManager shardManager;
    private final Duration heartbeatTimeout;
    private volatile long activeFencingToken = -1;

    public Coordinator(String nodeId, int shardCount, Duration leaseTtl, Duration heartbeatTimeout) {
        this.nodeId = nodeId;
        this.election = new LeaderElection(leaseTtl);
        this.shardManager = new ShardManager(shardCount, 50);
        this.heartbeatTimeout = heartbeatTimeout != null ? heartbeatTimeout : Duration.ofSeconds(10);
    }

    public String getNodeId() {
        return nodeId;
    }

    public ShardManager getShardManager() {
        return shardManager;
    }

    public boolean tryBecomeLeader(Instant now) {
        Optional<LeaderElection.Leadership> lead = election.tryAcquire(nodeId, now);
        if (lead.isPresent()) {
            activeFencingToken = lead.get().getFencingToken();
            return true;
        }
        return false;
    }

    public boolean renewLeadership(Instant now) {
        boolean ok = election.renew(nodeId, now);
        if (!ok) {
            activeFencingToken = -1;
        }
        return ok;
    }

    public boolean isLeader(Instant now) {
        return election.current(now)
                .map(l -> l.getNodeId().equals(nodeId))
                .orElse(false);
    }

    public long getActiveFencingToken() {
        return activeFencingToken;
    }

    public void registerWorker(String workerId, Instant now) {
        requireLeader(now);
        shardManager.registerWorker(workerId, now);
    }

    public void workerHeartbeat(String workerId, Instant now) {
        shardManager.heartbeat(workerId, now);
    }

    public Set<String> checkHealth(Instant now) {
        requireLeader(now);
        return shardManager.evictSilent(now, heartbeatTimeout);
    }

    public List<Integer> shardsFor(String workerId) {
        return shardManager.shardsOwnedBy(workerId);
    }

    public void resign(Instant now) {
        election.resign(nodeId);
        activeFencingToken = -1;
    }

    private void requireLeader(Instant now) {
        if (!isLeader(now)) {
            throw new IllegalStateException("node " + nodeId + " is not the leader");
        }
    }
}
