package com.jobscheduler.lld.coordinate;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Consistent-hash ring that maps job shards → worker / scheduler nodes.
 * <p>
 * Rebalances on join/leave with minimal key movement vs modulo remapping.
 */
public final class ConsistentHashRing {
    private final int virtualNodesPerWorker;
    private final SortedMap<Integer, String> ring = new TreeMap<>();
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public ConsistentHashRing(int virtualNodesPerWorker) {
        this.virtualNodesPerWorker = Math.max(1, virtualNodesPerWorker);
    }

    public void addWorker(String workerId) {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < virtualNodesPerWorker; i++) {
                ring.put(hash(workerId + "#" + i), workerId);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public void removeWorker(String workerId) {
        lock.writeLock().lock();
        try {
            for (int i = 0; i < virtualNodesPerWorker; i++) {
                ring.remove(hash(workerId + "#" + i));
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String ownerOf(String key) {
        lock.readLock().lock();
        try {
            if (ring.isEmpty()) {
                return null;
            }
            int h = hash(key);
            SortedMap<Integer, String> tail = ring.tailMap(h);
            int point = tail.isEmpty() ? ring.firstKey() : tail.firstKey();
            return ring.get(point);
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<String> workers() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(ring.values().stream().distinct().toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        lock.readLock().lock();
        try {
            return ring.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }

    /** FNV-1a 32-bit — spreads better than String.hashCode for small rings. */
    private static int hash(String key) {
        Objects.requireNonNull(key);
        int h = 0x811c9dc5;
        for (int i = 0; i < key.length(); i++) {
            h ^= key.charAt(i);
            h *= 0x01000193;
        }
        return h;
    }
}
