package com.jobscheduler.lld.demo;

import com.jobscheduler.lld.job.CatchUpPolicy;
import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.OverlapPolicy;
import com.jobscheduler.lld.job.RetryPolicy;
import com.jobscheduler.lld.job.ScheduleSpec;
import com.jobscheduler.lld.schedule.CronExpression;

import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;

/**
 * Recurring cron with time-wheel due selection (near-term in memory).
 */
public final class TimeWheelScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- TimeWheel / recurring ---");
        fx.bootstrapWorkers("w1", "w2");

        Instant now = fx.clock.now();
        // Fire every minute — nextAfter from slightly before now
        Job job = fx.api.scheduleJob(
                "fleet-hex-stats",
                "tenant",
                "recalc-hex-stats",
                ScheduleSpec.recurring("*/1 * * * *", ZoneId.of("UTC")),
                OverlapPolicy.SKIP,
                CatchUpPolicy.SKIP,
                RetryPolicy.defaults(),
                10);

        Instant next = job.getNextRunAt();
        System.out.println("job nextRunAt=" + next);

        // Force due by rewriting nextRunAt into the past (simulates clock advance)
        job.setNextRunAt(now.minusSeconds(1));
        fx.workerPool.refreshAllScanners();

        var runs = fx.workerPool.tick(fx.clock.now());
        System.out.println("due fires=" + runs.size()
                + ", rearmed next=" + job.getNextRunAt()
                + ", cron=" + new CronExpression("*/1 * * * *").getExpr());
        System.out.println("status: " + fx.api.getJobStatus(job.getJobId()));
    }
}
