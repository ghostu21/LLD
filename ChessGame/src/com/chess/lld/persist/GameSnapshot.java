package com.chess.lld.persist;

import java.util.ArrayList;
import java.util.List;

/**
 * Serializable match snapshot (moves + clocks), not a live {@code Game}.
 */
public final class GameSnapshot {
    private final String gameId;
    private final List<int[]> moves;
    private final long moveTimeMillis;

    public GameSnapshot(String gameId, List<int[]> moves, long moveTimeMillis) {
        this.gameId = gameId;
        this.moves = new ArrayList<>(moves);
        this.moveTimeMillis = moveTimeMillis;
    }

    public String getGameId() {
        return gameId;
    }

    public List<int[]> getMoves() {
        return moves;
    }

    public long getMoveTimeMillis() {
        return moveTimeMillis;
    }
}
