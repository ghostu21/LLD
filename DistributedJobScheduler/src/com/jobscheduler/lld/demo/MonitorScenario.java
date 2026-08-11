package com.jobscheduler.lld.demo;

import com.jobscheduler.lld.job.Job;
import com.jobscheduler.lld.job.ScheduleSpec;

/**
 * Bonus monitoring dashboard: missed executions + scheduling drift.
 */
public final class MonitorScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Monitoring Dashboard ---");
        fx.bootstrapWorkers("w1", "w2");

        for (int i = 0; i < 5; i++) {
            Job job = fx.api.scheduleJob(
                    "drift-" + i,
                    "payload-" + i,
                    ScheduleSpec.oneOff(fx.clock.now().plusSeconds(60)));
            // Make overdue by different amounts to create drift samples
            job.setNextRunAt(fx.clock.now().minusSeconds(i + 1));
        }
        fx.workerPool.refreshAllScanners();
        fx.workerPool.tick(fx.clock.now());

        System.out.println(fx.api.renderDashboard());
    }
}
