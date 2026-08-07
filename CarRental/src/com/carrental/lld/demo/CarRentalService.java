package com.carrental.lld.demo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CLI entry point that runs LLD feature demos (all, one, or list).
 * <p>
 * Why: each scenario maps to an interview pitfall (concurrent reserve, overlap,
 * async payment, cancellation policy, late fees, etc.).
 * <pre>
 *   java -cp out com.carrental.lld.demo.CarRentalService
 *   java -cp out com.carrental.lld.demo.CarRentalService reserve
 *   java -cp out com.carrental.lld.demo.CarRentalService list
 * </pre>
 */
public class CarRentalService {

    private static final Map<String, FeatureScenario> SCENARIOS = new LinkedHashMap<>();

    static {
        SCENARIOS.put("inventory", new InventoryScenario());
        SCENARIOS.put("reserve", new ReserveScenario());
        SCENARIOS.put("overlap", new OverlapScenario());
        SCENARIOS.put("addon", new AddonScenario());
        SCENARIOS.put("payment", new PaymentScenario());
        SCENARIOS.put("log", new LogScenario());
        SCENARIOS.put("notify", new NotifyScenario());
        SCENARIOS.put("cancel", new CancelScenario());
        SCENARIOS.put("return", new ReturnScenario());
        SCENARIOS.put("member", new MemberScenario());
    }

    /**
     * Dispatches CLI args: no args → all; list → print keys; name → one scenario.
     */
    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "list".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        DemoFixtures fx = new DemoFixtures();
        System.out.println("=== Car Rental LLD Demo ===\n");

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
        System.out.println("Usage: java com.carrental.lld.demo.CarRentalService [scenario|list]");
        System.out.println("Scenarios:");
        for (String key : SCENARIOS.keySet()) {
            System.out.println("  " + key);
        }
    }
}
