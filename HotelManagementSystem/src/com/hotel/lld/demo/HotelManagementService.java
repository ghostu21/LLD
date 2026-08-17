package com.hotel.lld.demo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CLI entry point that runs Hotel Management LLD feature demos.
 * <pre>
 *   java -cp out com.hotel.lld.demo.HotelManagementService
 *   java -cp out com.hotel.lld.demo.HotelManagementService book
 *   java -cp out com.hotel.lld.demo.HotelManagementService list
 * </pre>
 */
public class HotelManagementService {

    private static final Map<String, FeatureScenario> SCENARIOS = new LinkedHashMap<>();

    static {
        SCENARIOS.put("search", new SearchScenario());
        SCENARIOS.put("book", new BookScenario());
        SCENARIOS.put("overlap", new OverlapScenario());
        SCENARIOS.put("cancel", new CancelScenario());
        SCENARIOS.put("payment", new PaymentScenario());
        SCENARIOS.put("notify", new NotifyScenario());
        SCENARIOS.put("housekeeping", new HousekeepingScenario());
        SCENARIOS.put("guest", new GuestQueryScenario());
        SCENARIOS.put("concurrent", new ConcurrentScenario());
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "list".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        DemoFixtures fx = new DemoFixtures();
        System.out.println("=== Hotel Management System LLD Demo ===\n");

        if (args.length == 0) {
            for (Map.Entry<String, FeatureScenario> e : SCENARIOS.entrySet()) {
                e.getValue().run(fx);
                System.out.println();
            }
            fx.eventBus.shutdown();
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
        scenario.run(fx);
        fx.eventBus.shutdown();
        System.out.println("\n=== Done: " + name + " ===");
    }

    private static void printUsage() {
        System.out.println("Usage: java com.hotel.lld.demo.HotelManagementService [scenario|list]");
        System.out.println("Scenarios:");
        for (String key : SCENARIOS.keySet()) {
            System.out.println("  " + key);
        }
    }
}
