package com.amazon.lld.demo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CLI entry point that runs LLD feature demos (all, one, or list).
 * <p>
 * Why: each scenario maps to an interview pitfall (cart versioning, async bus,
 * payment strategy, shipment polling, etc.).
 * <pre>
 *   java -cp out com.amazon.lld.demo.AmazonEcommerceService
 *   java -cp out com.amazon.lld.demo.AmazonEcommerceService cart
 *   java -cp out com.amazon.lld.demo.AmazonEcommerceService list
 * </pre>
 */
public class AmazonEcommerceService {

    private static final Map<String, FeatureScenario> SCENARIOS = new LinkedHashMap<>();

    static {
        SCENARIOS.put("access", new AccessScenario());
        SCENARIOS.put("catalog", new CatalogScenario());
        SCENARIOS.put("cart", new CartScenario());
        SCENARIOS.put("checkout", new CheckoutScenario());
        SCENARIOS.put("payment", new PaymentScenario());
        SCENARIOS.put("shipping", new ShippingScenario());
        SCENARIOS.put("notify", new NotifyScenario());
        SCENARIOS.put("returns", new ReturnsScenario());
        SCENARIOS.put("cancel", new CancelScenario());
        SCENARIOS.put("review", new ReviewScenario());
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
        System.out.println("=== Amazon Ecommerce LLD Demo ===\n");

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
        System.out.println("Usage: java com.amazon.lld.demo.AmazonEcommerceService [scenario|list]");
        System.out.println("Scenarios:");
        for (String key : SCENARIOS.keySet()) {
            System.out.println("  " + key);
        }
    }
}
