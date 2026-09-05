package com.chess.lld.board;

import java.util.Objects;

/**
 * Board coordinate (file x 0–7 = a–h, rank y 0–7 = 1–8).
 * <p>
 * Why: attack maps and Zobrist keys need a hashable square, not a raw pair.
 */
public final class Position {
    private final int x;
    private final int y;

    public Position(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int index() {
        return y * 8 + x;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        Position position = (Position) o;
        return x == position.x && y == position.y;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public String toString() {
        return "" + (char) ('a' + x) + (y + 1);
    }
}
