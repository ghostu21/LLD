package com.jobscheduler.lld.demo;

import com.jobscheduler.lld.api.JobSchedulerApi;
import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.JobExecution;
import com.jobscheduler.lld.job.ScheduleSpec;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * scheduleJob / getJobStatus for a one-off runAt job.
 */
public final class ScheduleScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Schedule (one-off) ---");
        fx.bootstrapWorkers("w1", "w2");

        Instant runAt = fx.clock.now().plusSeconds(1);
        Job job = fx.api.scheduleJob("one-off-1", "dispatch:ride:42", ScheduleSpec.oneOff(runAt));
        System.out.println("scheduled: " + job.getJobId() + " next=" + job.getNextRunAt());

        // Simulate time advancing past due
        sleep(1100);
        List<JobExecution> runs = fx.workerPool.tick(fx.clock.now());
        System.out.println("tick fired " + runs.size() + " execution(s)");

        JobSchedulerApi.JobStatusView status = fx.api.getJobStatus("one-off-1");
        System.out.println(status);
    }

    static void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
