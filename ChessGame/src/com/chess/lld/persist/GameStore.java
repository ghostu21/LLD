package com.chess.lld.persist;

import com.chess.lld.game.Game;
import com.chess.lld.manager.GameManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory save/load by replaying move commands.
 * <p>
 * Why: LLD stand-in for Redis/Postgres; production would persist FEN + move
 * list or a binary board blob keyed by gameId.
 */
public final class GameStore {
    private static final GameStore INSTANCE = new GameStore();
    private final Map<String, GameSnapshot> store = new ConcurrentHashMap<>();

    private GameStore() {
    }

    public static GameStore getInstance() {
        return INSTANCE;
    }

    public void save(Game game) {
        store.put(game.getGameId(), new GameSnapshot(game.getGameId(), game.moveCoords(), 0L));
    }

    public Game load(String gameId) {
        GameSnapshot snapshot = store.get(gameId);
        if (snapshot == null) {
            return null;
        }
        Game restored = new Game(gameId + "-restored");
        for (int[] m : snapshot.getMoves()) {
            restored.makeMove(m[0], m[1], m[2], m[3]);
        }
        GameManager.getInstance().createGame(restored);
        return restored;
    }
}
