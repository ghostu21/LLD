package com.jobscheduler.lld.demo;

import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.JobStatus;
import com.jobscheduler.lld.job.ScheduleSpec;

import java.time.ZoneId;

/**
 * Recurring cron + cancelJob.
 */
public final class CancelScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Cancel ---");
        fx.bootstrapWorkers("w1");

        Job job = fx.api.scheduleJob(
                "cron-cancel",
                "heartbeat",
                ScheduleSpec.recurring("*/1 * * * *", ZoneId.of("UTC")));
        System.out.println("created recurring job status=" + job.getStatus());

        boolean cancelled = fx.api.cancelJob("cron-cancel");
        System.out.println("cancelJob => " + cancelled
                + ", status=" + fx.api.getJobStatus("cron-cancel").getStatus());

        fx.workerPool.tick(fx.clock.now());
        System.out.println("after tick, still CANCELLED? "
                + (fx.api.getJobStatus("cron-cancel").getStatus() == JobStatus.CANCELLED));
    }
}
