package com.chess.lld.strategy;

import com.chess.lld.board.Board;

/**
 * Rook: orthogonal sliding move.
 */
public final class RookMoveStrategy implements MoveStrategy {
    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Board board) {
        if (startX != endX && startY != endY) {
            return false;
        }
        return board.isPathClear(startX, startY, endX, endY)
                && board.isCapturableOrEmpty(startX, startY, endX, endY);
    }
}
