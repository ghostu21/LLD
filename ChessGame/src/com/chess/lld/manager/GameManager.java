package com.chess.lld.manager;

import com.chess.lld.game.Game;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Process-wide registry of games (Singleton).
 * <p>
 * Why: matchmaking and lookups need one map; concurrency still lives
 * <em>inside</em> each {@link Game} — never a global move lock.
 */
public final class GameManager {
    private static final GameManager INSTANCE = new GameManager();
    private final Map<String, Game> games = new ConcurrentHashMap<>();

    private GameManager() {
    }

    public static GameManager getInstance() {
        return INSTANCE;
    }

    public String createGame() {
        return createGame(0L);
    }

    public String createGame(long moveTimeMillis) {
        String gameId = UUID.randomUUID().toString();
        games.put(gameId, new Game(gameId, moveTimeMillis));
        return gameId;
    }

    public String createGame(Game game) {
        games.put(game.getGameId(), game);
        return game.getGameId();
    }

    public Game getGame(String gameId) {
        return games.get(gameId);
    }

    public void terminateGame(String gameId) {
        Game game = games.remove(gameId);
        if (game != null) {
            game.close();
        }
    }
}
