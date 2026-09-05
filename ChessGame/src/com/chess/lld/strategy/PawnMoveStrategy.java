package com.chess.lld.strategy;

import com.chess.lld.board.Board;
import com.chess.lld.board.Position;
import com.chess.lld.piece.Color;
import com.chess.lld.piece.Piece;

/**
 * Pawn: one forward, two from home rank, diagonal capture, en passant.
 */
public final class PawnMoveStrategy implements MoveStrategy {
    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Board board) {
        Piece piece = board.getPiece(startX, startY);
        if (piece == null) {
            return false;
        }
        int dir = piece.getColor() == Color.WHITE ? 1 : -1;
        int dy = endY - startY;
        int dx = endX - startX;
        if (!board.inBounds(endX, endY)) {
            return false;
        }

        if (dx == 0 && dy == dir && board.isEmpty(endX, endY)) {
            return true;
        }
        int home = piece.getColor() == Color.WHITE ? 1 : 6;
        if (dx == 0 && dy == 2 * dir && startY == home
                && board.isEmpty(startX, startY + dir)
                && board.isEmpty(endX, endY)) {
            return true;
        }
        if (Math.abs(dx) == 1 && dy == dir) {
            Piece dest = board.getPiece(endX, endY);
            if (dest != null && dest.getColor() != piece.getColor()) {
                return true;
            }
            Position ep = board.getEnPassantTarget();
            return ep != null && ep.getX() == endX && ep.getY() == endY;
        }
        return false;
    }
}
