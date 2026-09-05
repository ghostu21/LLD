package com.chess.lld.strategy;

import com.chess.lld.board.Board;

/**
 * Bishop: diagonal sliding move.
 */
public final class BishopMoveStrategy implements MoveStrategy {
    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Board board) {
        if (Math.abs(endX - startX) != Math.abs(endY - startY) || startX == endX) {
            return false;
        }
        return board.isPathClear(startX, startY, endX, endY)
                && board.isCapturableOrEmpty(startX, startY, endX, endY);
    }
}
