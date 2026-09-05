package com.chess.lld.strategy;

import com.chess.lld.board.Board;

/**
 * Queen: rook or bishop geometry.
 */
public final class QueenMoveStrategy implements MoveStrategy {
    private final MoveStrategy rook = new RookMoveStrategy();
    private final MoveStrategy bishop = new BishopMoveStrategy();

    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Board board) {
        return rook.isValidMove(startX, startY, endX, endY, board)
                || bishop.isValidMove(startX, startY, endX, endY, board);
    }
}
