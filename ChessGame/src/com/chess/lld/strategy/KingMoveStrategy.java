package com.chess.lld.strategy;

import com.chess.lld.board.Board;
import com.chess.lld.piece.Color;
import com.chess.lld.piece.Piece;
import com.chess.lld.piece.PieceType;

/**
 * King: one square, or castling when rights/path/safety allow.
 */
public final class KingMoveStrategy implements MoveStrategy {
    @Override
    public boolean isValidMove(int startX, int startY, int endX, int endY, Board board) {
        if (!board.inBounds(endX, endY)) {
            return false;
        }
        int dx = Math.abs(endX - startX);
        int dy = Math.abs(endY - startY);
        if (dx <= 1 && dy <= 1 && (dx + dy) > 0) {
            return board.isCapturableOrEmpty(startX, startY, endX, endY);
        }
        return isCastle(startX, startY, endX, endY, board);
    }

    private boolean isCastle(int startX, int startY, int endX, int endY, Board board) {
        Piece king = board.getPiece(startX, startY);
        if (king == null || king.getType() != PieceType.KING || endY != startY || Math.abs(endX - startX) != 2) {
            return false;
        }
        Color color = king.getColor();
        boolean kingSide = endX == 6;
        boolean queenSide = endX == 2;
        if (!kingSide && !queenSide) {
            return false;
        }
        if (startX != 4) {
            return false;
        }
        if (color == Color.WHITE && startY != 0) {
            return false;
        }
        if (color == Color.BLACK && startY != 7) {
            return false;
        }
        if (kingSide && !board.canCastleKingSide(color)) {
            return false;
        }
        if (queenSide && !board.canCastleQueenSide(color)) {
            return false;
        }
        int rookX = kingSide ? 7 : 0;
        Piece rook = board.getPiece(rookX, startY);
        if (rook == null || rook.getType() != PieceType.ROOK || rook.getColor() != color) {
            return false;
        }
        if (!board.isPathClear(startX, startY, rookX, startY)) {
            return false;
        }
        if (queenSide && !board.isEmpty(1, startY)) {
            return false;
        }
        Color enemy = color.opponent();
        int throughX = kingSide ? 5 : 3;
        return !board.isSquareAttacked(startX, startY, enemy)
                && !board.isSquareAttacked(throughX, startY, enemy)
                && !board.isSquareAttacked(endX, endY, enemy);
    }
}
