package com.chess.lld.piece;

/**
 * Side of a player or piece.
 */
public enum Color {
    WHITE,
    BLACK;

    /**
     * @return the opposing side
     */
    public Color opponent() {
        return this == WHITE ? BLACK : WHITE;
    }
}
