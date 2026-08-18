package com.reco.lld.demo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CLI entry point for Recommendation Service LLD demos.
 * <pre>
 *   java -cp out com.reco.lld.demo.RecommendationService
 *   java -cp out com.reco.lld.demo.RecommendationService personalize
 *   java -cp out com.reco.lld.demo.RecommendationService list
 * </pre>
 */
public class RecommendationService {

    private static final Map<String, FeatureScenario> SCENARIOS = new LinkedHashMap<>();

    static {
        SCENARIOS.put("auth", new AuthScenario());
        SCENARIOS.put("access", new AccessScenario());
        SCENARIOS.put("coldstart", new ColdStartScenario());
        SCENARIOS.put("personalize", new PersonalizeScenario());
        SCENARIOS.put("similar", new SimilarScenario());
        SCENARIOS.put("feedback", new FeedbackScenario());
        SCENARIOS.put("filter", new FilterScenario());
        SCENARIOS.put("rate", new RateLimitScenario());
        SCENARIOS.put("experiment", new ExperimentScenario());
        SCENARIOS.put("notify", new NotifyScenario());
        SCENARIOS.put("concurrent", new ConcurrentScenario());
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "list".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        DemoFixtures fx = new DemoFixtures();
        System.out.println("=== Recommendation Service LLD Demo ===\n");

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
        System.out.println("Usage: java com.reco.lld.demo.RecommendationService [scenario|list]");
        System.out.println("Scenarios:");
        for (String key : SCENARIOS.keySet()) {
            System.out.println("  " + key);
        }
    }
}
