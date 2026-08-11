package com.jobscheduler.lld.demo;

import com.jobscheduler.lld.job.CatchUpPolicy;
import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.OverlapPolicy;
import com.jobscheduler.lld.job.RetryPolicy;
import com.jobscheduler.lld.job.ScheduleSpec;

import java.time.Duration;
import java.time.ZoneId;

/**
 * Pause / resume + catch-up policy after simulated outage.
 */
public final class PauseCatchUpScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Pause / Resume / Catch-up ---");
        fx.bootstrapWorkers("w1");

        Job job = fx.api.scheduleJob(
                "monthly-report",
                "biz",
                "uber-for-business-report",
                ScheduleSpec.recurring("*/1 * * * *", ZoneId.of("UTC")),
                OverlapPolicy.SKIP,
                CatchUpPolicy.ONE,
                RetryPolicy.defaults(),
                5);

        fx.api.pauseJob("monthly-report");
        System.out.println("paused status=" + fx.api.getJobStatus("monthly-report").getStatus());

        // Simulate outage window: nextRunAt far in the past while paused
        job.setNextRunAt(fx.clock.now().minus(Duration.ofMinutes(5)));

        fx.api.resumeJob("monthly-report");
        System.out.println("resumed nextRunAt=" + job.getNextRunAt()
                + " (recomputed from now; catch-up applies when due scanner sees backlog)");

        // Force overdue to exercise catch-up ONE
        job.setNextRunAt(fx.clock.now().minus(Duration.ofMinutes(3)));
        fx.workerPool.refreshAllScanners();
        var runs = fx.workerPool.tick(fx.clock.now());
        System.out.println("catch-up fires=" + runs.size()
                + " (policy=ONE → at most one missed fire)");
    }
}
