package com.jobscheduler.lld.coordinate;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Simulated ZooKeeper / etcd leader election via a renewable lease.
 * <p>
 * Only the leader may assign shards. Fencing token increments on each new
 * leadership so stale leaders' claims are rejected.
 */
public final class LeaderElection {
    public static final class Leadership {
        private final String nodeId;
        private final long fencingToken;
        private volatile Instant leaseExpiresAt;

        Leadership(String nodeId, long fencingToken, Instant leaseExpiresAt) {
            this.nodeId = nodeId;
            this.fencingToken = fencingToken;
            this.leaseExpiresAt = leaseExpiresAt;
        }

        public String getNodeId() {
            return nodeId;
        }

        public long getFencingToken() {
            return fencingToken;
        }

        public Instant getLeaseExpiresAt() {
            return leaseExpiresAt;
        }

        void renew(Instant expiresAt) {
            this.leaseExpiresAt = expiresAt;
        }
    }

    private final Duration leaseTtl;
    private final AtomicReference<Leadership> leader = new AtomicReference<>();
    private final AtomicLong tokenSeq = new AtomicLong(0);

    public LeaderElection(Duration leaseTtl) {
        this.leaseTtl = leaseTtl != null ? leaseTtl : Duration.ofSeconds(15);
    }

    public synchronized Optional<Leadership> tryAcquire(String nodeId, Instant now) {
        Leadership current = leader.get();
        if (current != null && current.leaseExpiresAt.isAfter(now)
                && !current.nodeId.equals(nodeId)) {
            return Optional.empty();
        }
        if (current != null && current.nodeId.equals(nodeId)
                && current.leaseExpiresAt.isAfter(now)) {
            current.renew(now.plus(leaseTtl));
            return Optional.of(current);
        }
        long token = tokenSeq.incrementAndGet();
        Leadership next = new Leadership(nodeId, token, now.plus(leaseTtl));
        leader.set(next);
        return Optional.of(next);
    }

    public synchronized boolean renew(String nodeId, Instant now) {
        Leadership current = leader.get();
        if (current == null || !current.nodeId.equals(nodeId)) {
            return false;
        }
        if (!current.leaseExpiresAt.isAfter(now)) {
            return false;
        }
        current.renew(now.plus(leaseTtl));
        return true;
    }

    public synchronized Optional<Leadership> current(Instant now) {
        Leadership current = leader.get();
        if (current == null || !current.leaseExpiresAt.isAfter(now)) {
            return Optional.empty();
        }
        return Optional.of(current);
    }

    public synchronized void resign(String nodeId) {
        Leadership current = leader.get();
        if (current != null && current.nodeId.equals(nodeId)) {
            leader.set(null);
        }
    }
}
