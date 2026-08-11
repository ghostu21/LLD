package com.jobscheduler.lld.store;

import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.JobStatus;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Durable job registry (in-memory stand-in for Postgres / DynamoDB).
 * <p>
 * Access patterns:
 * <ul>
 *   <li>PK lookup by jobId</li>
 *   <li>Range by nextRunAt within a shard for due scanning</li>
 * </ul>
 */
public final class JobStore {
    private final Map<String, Job> jobs = new ConcurrentHashMap<>();

    public void save(Job job) {
        jobs.put(job.getJobId(), job);
    }

    public Optional<Job> findById(String jobId) {
        return Optional.ofNullable(jobs.get(jobId));
    }

    public boolean delete(String jobId) {
        return jobs.remove(jobId) != null;
    }

    public List<Job> findDueInShard(int shardId, int shardCount, Instant now, int limit) {
        return jobs.values().stream()
                .filter(j -> j.getStatus() == JobStatus.ACTIVE)
                .filter(j -> shardOf(j.getShardKey(), shardCount) == shardId)
                .filter(j -> j.getNextRunAt() != null && !j.getNextRunAt().isAfter(now))
                .sorted(Comparator.comparing(Job::getNextRunAt)
                        .thenComparing(Job::getPriority, Comparator.reverseOrder()))
                .limit(limit)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Job> findActiveInShard(int shardId, int shardCount) {
        return jobs.values().stream()
                .filter(j -> j.getStatus() == JobStatus.ACTIVE)
                .filter(j -> shardOf(j.getShardKey(), shardCount) == shardId)
                .collect(Collectors.toCollection(ArrayList::new));
    }

    public List<Job> findAll() {
        return new ArrayList<>(jobs.values());
    }

    public int size() {
        return jobs.size();
    }

    public static int shardOf(String shardKey, int shardCount) {
        int h = shardKey.hashCode();
        return Math.floorMod(h, shardCount);
    }
}
