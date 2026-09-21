package com.vending.lld.demo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CLI entry for vending-machine LLD scenarios.
 * <pre>
 *   java -cp out com.vending.lld.demo.VendingMachineService
 *   java -cp out com.vending.lld.demo.VendingMachineService purchase
 *   java -cp out com.vending.lld.demo.VendingMachineService list
 * </pre>
 */
public final class VendingMachineService {
    private static final Map<String, FeatureScenario> SCENARIOS = new LinkedHashMap<>();

    static {
        SCENARIOS.put("purchase", new PurchaseScenario());
        SCENARIOS.put("card", new CardScenario());
        SCENARIOS.put("insufficient", new InsufficientScenario());
        SCENARIOS.put("stock", new StockScenario());
        SCENARIOS.put("expiry", new ExpiryScenario());
        SCENARIOS.put("refund", new RefundScenario());
        SCENARIOS.put("discount", new DiscountScenario());
        SCENARIOS.put("concurrency", new ConcurrencyScenario());
        SCENARIOS.put("admin", new AdminScenario());
        SCENARIOS.put("events", new EventsScenario());
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "list".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }
        System.out.println("=== Vending Machine LLD Demo ===\n");
        if (args.length == 0) {
            for (Map.Entry<String, FeatureScenario> e : SCENARIOS.entrySet()) {
                DemoFixtures fx = new DemoFixtures();
                try {
                    e.getValue().run(fx);
                    fx.settle();
                } finally {
                    fx.close();
                }
                System.out.println();
            }
            System.out.println("=== All scenarios complete ===");
            return;
        }
        FeatureScenario scenario = SCENARIOS.get(args[0].toLowerCase());
        if (scenario == null) {
            System.err.println("Unknown scenario: " + args[0]);
            printUsage();
            System.exit(1);
            return;
        }
        DemoFixtures fx = new DemoFixtures();
        try {
            scenario.run(fx);
            fx.settle();
        } finally {
            fx.close();
        }
        System.out.println("\n=== Done: " + args[0] + " ===");
    }

    private static void printUsage() {
        System.out.println("Usage: java com.vending.lld.demo.VendingMachineService [scenario|list]");
        System.out.println("Scenarios:");
        for (String key : SCENARIOS.keySet()) {
            System.out.println("  " + key);
        }
    }
}
