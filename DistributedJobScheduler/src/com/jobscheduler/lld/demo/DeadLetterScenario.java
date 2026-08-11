package com.jobscheduler.lld.demo;

import com.jobscheduler.lld.job.CatchUpPolicy;
import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.OverlapPolicy;
import com.jobscheduler.lld.job.RetryPolicy;
import com.jobscheduler.lld.job.ScheduleSpec;

import java.time.Duration;
import java.time.ZoneId;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Exhaust retries → dead letter queue (no infinite retry).
 */
public final class DeadLetterScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Dead Letter Queue ---");
        AtomicInteger attempts = new AtomicInteger();
        fx.workerPool.setJobHandler((job, exec) -> {
            attempts.incrementAndGet();
            throw new RuntimeException("downstream 500");
        });
        fx.bootstrapWorkers("w1");

        RetryPolicy tight = new RetryPolicy(2, Duration.ofMillis(1), Duration.ofMillis(10), 2.0);
        Job job = fx.api.scheduleJob(
                "poison",
                "tenant",
                "bad-payload",
                ScheduleSpec.oneOff(fx.clock.now().plusSeconds(30)),
                OverlapPolicy.ALLOW,
                CatchUpPolicy.SKIP,
                tight,
                0);
        job.setNextRunAt(fx.clock.now().minusSeconds(1));
        fx.workerPool.refreshAllScanners();

        fx.workerPool.tick(fx.clock.now());
        // Manually retry failed execution path by re-firing same idempotency window
        // Second attempt via reclaim-style re-execute
        var execs = fx.executionStore.findByJobId("poison");
        System.out.println("after first failure: status=" + execs.get(0).getStatus()
                + " attempt=" + execs.get(0).getAttempt());

        // Force another fire with a *new* scheduled time so a new idem key is used,
        // simulating distinct retries after re-arm — for same key, mark failed then retry API
        var executor = fx.workerPool.listWorkers().get(0).getExecutor();
        var intent = new com.jobscheduler.lld.schedule.DueScanner.FireIntent(
                "poison", execs.get(0).getScheduledFireAt(), false);
        var second = executor.execute(intent, fx.coordinator.getActiveFencingToken(), fx.clock.now());
        System.out.println("retry status=" + second.getStatus()
                + " attempt=" + second.getAttempt()
                + " dlqSize=" + fx.deadLetterQueue.size());
        System.out.println("handler attempts observed=" + attempts.get());
    }
}
