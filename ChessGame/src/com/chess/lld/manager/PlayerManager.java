package com.chess.lld.manager;

import com.chess.lld.game.Game;
import com.chess.lld.piece.Color;
import com.chess.lld.player.Player;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Player-to-game seating (Singleton).
 * First assignee is White, second is Black.
 */
public final class PlayerManager {
    private static final PlayerManager INSTANCE = new PlayerManager();
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, String> playerToGame = new ConcurrentHashMap<>();

    private PlayerManager() {
    }

    public static PlayerManager getInstance() {
        return INSTANCE;
    }

    public Player getOrCreate(String playerId) {
        return players.computeIfAbsent(playerId, Player::new);
    }

    public void assignPlayerToGame(String playerId, String gameId) {
        Game game = GameManager.getInstance().getGame(gameId);
        if (game == null) {
            throw new IllegalArgumentException("Unknown game " + gameId);
        }
        Player player = getOrCreate(playerId);
        Color color = game.getWhitePlayer() == null ? Color.WHITE : Color.BLACK;
        game.seat(player, color);
        playerToGame.put(playerId, gameId);
    }

    public String getGameIdForPlayer(String playerId) {
        return playerToGame.get(playerId);
    }
}
