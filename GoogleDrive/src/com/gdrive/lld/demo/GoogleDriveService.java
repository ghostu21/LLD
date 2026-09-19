package com.gdrive.lld.demo;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CLI: all scenarios, one name, or list.
 * <pre>
 *   java -cp out com.gdrive.lld.demo.GoogleDriveService
 *   java -cp out com.gdrive.lld.demo.GoogleDriveService share
 *   java -cp out com.gdrive.lld.demo.GoogleDriveService list
 * </pre>
 */
public final class GoogleDriveService {
    private static final Map<String, FeatureScenario> SCENARIOS = new LinkedHashMap<>();

    static {
        SCENARIOS.put("auth", new AuthScenario());
        SCENARIOS.put("tree", new TreeScenario());
        SCENARIOS.put("share", new ShareScenario());
        SCENARIOS.put("proxy", new ProxyScenario());
        SCENARIOS.put("version", new VersionScenario());
        SCENARIOS.put("search", new SearchScenario());
        SCENARIOS.put("quota", new QuotaScenario());
        SCENARIOS.put("concurrency", new ConcurrencyScenario());
        SCENARIOS.put("move", new MoveScenario());
        SCENARIOS.put("undo", new UndoScenario());
        SCENARIOS.put("observer", new ObserverScenario());
        SCENARIOS.put("storage", new StorageScenario());
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "list".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }
        System.out.println("=== Google Drive LLD Demo ===\n");
        if (args.length == 0) {
            for (Map.Entry<String, FeatureScenario> e : SCENARIOS.entrySet()) {
                e.getValue().run(new DemoFixtures());
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
        scenario.run(new DemoFixtures());
        System.out.println("\n=== Done: " + args[0] + " ===");
    }

    private static void printUsage() {
        System.out.println("Usage: java com.gdrive.lld.demo.GoogleDriveService [scenario|list]");
        System.out.println("Scenarios:");
        for (String key : SCENARIOS.keySet()) {
            System.out.println("  " + key);
        }
    }
}
