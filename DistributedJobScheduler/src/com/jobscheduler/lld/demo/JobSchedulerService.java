package com.jobscheduler.lld.demo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CLI entry point for Distributed Job Scheduler LLD demos.
 * <pre>
 *   java -cp out com.jobscheduler.lld.demo.JobSchedulerService
 *   java -cp out com.jobscheduler.lld.demo.JobSchedulerService lease
 *   java -cp out com.jobscheduler.lld.demo.JobSchedulerService list
 * </pre>
 */
public class JobSchedulerService {

    private static final Map<String, FeatureScenario> SCENARIOS = new LinkedHashMap<>();

    static {
        SCENARIOS.put("schedule", new ScheduleScenario());
        SCENARIOS.put("cancel", new CancelScenario());
        SCENARIOS.put("timewheel", new TimeWheelScenario());
        SCENARIOS.put("lease", new LeaseIdempotencyScenario());
        SCENARIOS.put("dlq", new DeadLetterScenario());
        SCENARIOS.put("shard", new ShardRebalanceScenario());
        SCENARIOS.put("catchup", new PauseCatchUpScenario());
        SCENARIOS.put("monitor", new MonitorScenario());
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "list".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        System.out.println("=== Distributed Job Scheduler LLD Demo ===\n");

        if (args.length == 0) {
            for (Map.Entry<String, FeatureScenario> e : SCENARIOS.entrySet()) {
                DemoFixtures fx = new DemoFixtures();
                e.getValue().run(fx);
                System.out.println();
            }
            System.out.println("=== All scenarios complete ===");
            return;
        }

        String name = args[0].toLowerCase();
        FeatureScenario scenario = SCENARIOS.get(name);
        if (scenario == null) {
            System.err.println("Unknown scenario: " + args[0]);
            printUsage();
            System.exit(1);
            return;
        }
        DemoFixtures fx = new DemoFixtures();
        scenario.run(fx);
        System.out.println("\n=== Done: " + name + " ===");
    }

    private static void printUsage() {
        System.out.println("Usage: java com.jobscheduler.lld.demo.JobSchedulerService [scenario|list]");
        System.out.println("Scenarios:");
        for (String key : SCENARIOS.keySet()) {
            System.out.println("  " + key);
        }
    }
}
