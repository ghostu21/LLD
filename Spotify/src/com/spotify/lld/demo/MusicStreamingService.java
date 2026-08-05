package com.spotify.lld.demo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Entry point. Run all scenarios, or one by name:
 * <pre>
 *   java com.spotify.lld.demo.MusicStreamingService
 *   java com.spotify.lld.demo.MusicStreamingService auth
 *   java com.spotify.lld.demo.MusicStreamingService list
 * </pre>
 */
public class MusicStreamingService {

    private static final Map<String, FeatureScenario> SCENARIOS = new LinkedHashMap<>();

    static {
        SCENARIOS.put("auth", new AuthScenario());
        SCENARIOS.put("session", new SessionPlaybackScenario());
        SCENARIOS.put("playlist", new PlaylistScenario());
        SCENARIOS.put("streaming", new StreamingScenario());
        SCENARIOS.put("catalog", new CatalogSearchScenario());
        SCENARIOS.put("license", new LicensingScenario());
        SCENARIOS.put("recommend", new RecommendationScenario());
        SCENARIOS.put("social", new SocialGraphScenario());
        SCENARIOS.put("events", new EventBusScenario());
        SCENARIOS.put("offline", new OfflineModeScenario());
        SCENARIOS.put("limits", new LimitsScenario());
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "list".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        DemoFixtures fx = new DemoFixtures();
        System.out.println("=== Spotify LLD Demo ===\n");

        if (args.length == 0) {
            for (Map.Entry<String, FeatureScenario> e : SCENARIOS.entrySet()) {
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
        scenario.run(fx);
        System.out.println("\n=== Done: " + name + " ===");
    }

    private static void printUsage() {
        System.out.println("Usage: java com.spotify.lld.demo.MusicStreamingService [scenario|list]");
        System.out.println("Scenarios:");
        for (String key : SCENARIOS.keySet()) {
            System.out.println("  " + key);
        }
    }
}
