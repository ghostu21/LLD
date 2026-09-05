package com.chess.lld.player;

/**
 * Observer of game-state changes (moves, check, terminal results).
 * <p>
 * Why: players (and later websockets) subscribe without Game knowing transports.
 */
public interface GameObserver {
    void update(String gameId, String message);
}
