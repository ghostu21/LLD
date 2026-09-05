package com.chess.lld.player;

import com.chess.lld.piece.Color;

/**
 * Player is an observer; color is assigned when seated at a game.
 */
public final class Player implements GameObserver {
    private final String playerId;
    private Color color;

    public Player(String playerId) {
        this.playerId = playerId;
    }

    public String getPlayerId() {
        return playerId;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    @Override
    public void update(String gameId, String message) {
        System.out.println("Player " + playerId + " [" + gameId + "]: " + message);
    }
}
