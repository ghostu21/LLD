package com.chess.lld.piece;

import com.chess.lld.board.Board;
import com.chess.lld.strategy.MoveStrategy;

/**
 * Extrinsic piece instance on a board: shared properties + movement strategy.
 * <p>
 * Why: Strategy keeps piece classes from exploding into Pawn/Rook/...
 * subclasses just to vary move rules; Flyweight properties stay shared.
 */
public final class Piece {
    private final PieceProperties properties;
    private final MoveStrategy moveStrategy;

    public Piece(PieceProperties properties, MoveStrategy moveStrategy) {
        this.properties = properties;
        this.moveStrategy = moveStrategy;
    }

    public PieceProperties getProperties() {
        return properties;
    }

    public PieceType getType() {
        return properties.getType();
    }

    public Color getColor() {
        return properties.getColor();
    }

    /**
     * Geometric / piece-rule validation only (not king-safety).
     */
    public boolean isValidMove(int startX, int startY, int endX, int endY, Board board) {
        return moveStrategy.isValidMove(startX, startY, endX, endY, board);
    }

    /**
     * ASCII symbol: white uppercase, black lowercase.
     */
    public char symbol() {
        char c = switch (getType()) {
            case PAWN -> 'P';
            case ROOK -> 'R';
            case KNIGHT -> 'N';
            case BISHOP -> 'B';
            case QUEEN -> 'Q';
            case KING -> 'K';
        };
        return getColor() == Color.WHITE ? c : Character.toLowerCase(c);
    }
}
