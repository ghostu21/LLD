package com.chess.lld.piece;

/**
 * Intrinsic, immutable piece state shared across games (Flyweight).
 * <p>
 * Why: millions of games would otherwise duplicate the same (PAWN, WHITE)
 * objects. Color and type never change for a given flyweight key.
 */
public final class PieceProperties {
    private final PieceType type;
    private final Color color;

    PieceProperties(PieceType type, Color color) {
        this.type = type;
        this.color = color;
    }

    public PieceType getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }
}
