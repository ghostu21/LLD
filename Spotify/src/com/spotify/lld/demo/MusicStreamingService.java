package com.spotify.lld.demo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CLI entry point that runs LLD feature demos (all, one, or list).
 * <p>
 * Why: each scenario maps to an interview pitfall (auth, session isolation,
 * licensing, limits, etc.) so you can walk the fix aloud.
 * <p>
 * Logic: static registry of name → {@link FeatureScenario}; main builds shared
 * {@link DemoFixtures}, then runs all scenarios or the named one.
 * <pre>
 *   java com.spotify.lld.demo.MusicStreamingService
 *   java com.spotify.lld.demo.MusicStreamingService auth
 *   java com.spotify.lld.demo.MusicStreamingService list
 * </pre>
 */
public class MusicStreamingService {

    /** Ordered map so "run all" prints scenarios in a stable interview-friendly order. */
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

    /**
     * Dispatches CLI args:
     * <ul>
     *   <li>no args → run every scenario</li>
     *   <li>{@code list} → print scenario names</li>
     *   <li>name → run that scenario only</li>
     * </ul>
     */
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

    /** Prints usage and the registered scenario keys. */
    private static void printUsage() {
        System.out.println("Usage: java com.spotify.lld.demo.MusicStreamingService [scenario|list]");
        System.out.println("Scenarios:");
        for (String key : SCENARIOS.keySet()) {
            System.out.println("  " + key);
        }
    }
}
