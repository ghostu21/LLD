package com.chess.lld.strategy;

import com.chess.lld.board.Board;

/**
 * Knight: L-shape, jumps.
 */
public final class KnightMoveStrategy implements MoveStrategy {
    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Board board) {
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);
        boolean lShape = (dx == 1 && dy == 2) || (dx == 2 && dy == 1);
        return lShape && board.isCapturableOrEmpty(startX, startY, endX, endY);
    }
}
