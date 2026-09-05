package com.chess.lld.demo;

import com.chess.lld.game.Game;
import com.chess.lld.manager.GameManager;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CLI entry: all scenarios, one name, or list.
 * <pre>
 *   java -cp out com.chess.lld.demo.ChessGameService
 *   java -cp out com.chess.lld.demo.ChessGameService checkmate
 *   java -cp out com.chess.lld.demo.ChessGameService list
 * </pre>
 */
public final class ChessGameService {
    private static final Map<String, FeatureScenario> SCENARIOS = new LinkedHashMap<>();

    static {
        SCENARIOS.put("move", new MoveScenario());
        SCENARIOS.put("concurrency", new ConcurrencyScenario());
        SCENARIOS.put("undo", new UndoScenario());
        SCENARIOS.put("checkmate", new CheckmateScenario());
        SCENARIOS.put("stalemate", new StalemateScenario());
        SCENARIOS.put("draw", new DrawScenario());
        SCENARIOS.put("repetition", new RepetitionScenario());
        SCENARIOS.put("timeout", new TimeoutScenario());
        SCENARIOS.put("observer", new ObserverScenario());
        SCENARIOS.put("flyweight", new FlyweightScenario());
        SCENARIOS.put("persist", new PersistScenario());
    }

    public static void main(String[] args) throws Exception {
        if (args.length > 0 && "list".equalsIgnoreCase(args[0])) {
            printUsage();
            return;
        }

        System.out.println("=== Chess Game LLD Demo ===\n");
        if (args.length == 0) {
            for (Map.Entry<String, FeatureScenario> e : SCENARIOS.entrySet()) {
                e.getValue().run();
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
        scenario.run();
        System.out.println("\n=== Done: " + args[0] + " ===");
    }

    static Game newGame() {
        String id = GameManager.getInstance().createGame();
        return GameManager.getInstance().getGame(id);
    }

    private static void printUsage() {
        System.out.println("Usage: java com.chess.lld.demo.ChessGameService [scenario|list]");
        System.out.println("Scenarios:");
        for (String key : SCENARIOS.keySet()) {
            System.out.println("  " + key);
        }
    }
}
