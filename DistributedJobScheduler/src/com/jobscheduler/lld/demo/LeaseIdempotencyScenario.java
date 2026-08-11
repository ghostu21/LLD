package com.jobscheduler.lld.demo;

import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.JobExecution;
import com.jobscheduler.lld.job.ScheduleSpec;
import com.jobscheduler.lld.schedule.DueScanner;
import com.jobscheduler.lld.worker.JobExecutor;

import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Lease pull + idempotency key prevents double-execute on retry.
 */
public final class LeaseIdempotencyScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Lease + Idempotency ---");
        AtomicInteger handlerCalls = new AtomicInteger();
        fx.workerPool.setJobHandler((job, exec) -> handlerCalls.incrementAndGet());
        fx.bootstrapWorkers("w1");

        Instant fireAt = fx.clock.now().plusMillis(100);
        Job job = fx.api.scheduleJob("idem-1", "charge:customer:9", ScheduleSpec.oneOff(fireAt));
        job.setNextRunAt(fx.clock.now().minusSeconds(1));
        fx.workerPool.refreshAllScanners();

        var first = fx.workerPool.tick(fx.clock.now());
        System.out.println("first tick executions=" + first.size()
                + " handlerCalls=" + handlerCalls.get());

        // Simulate redelivery of the same fire intent
        JobExecutor executor = fx.workerPool.listWorkers().get(0).getExecutor();
        DueScanner.FireIntent dup = new DueScanner.FireIntent(
                job.getJobId(), first.get(0).getScheduledFireAt(), false);
        JobExecution again = executor.execute(dup, fx.coordinator.getActiveFencingToken(), fx.clock.now());
        System.out.println("duplicate delivery status=" + again.getStatus()
                + " sameExecutionId=" + again.getExecutionId().equals(first.get(0).getExecutionId())
                + " handlerCalls(still)=" + handlerCalls.get());
    }
}
