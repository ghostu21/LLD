package com.chess.lld.move;

import com.chess.lld.board.Board;
import com.chess.lld.board.Position;
import com.chess.lld.piece.Piece;

/**
 * Command for one ply: execute / undo with enough state to reverse specials.
 * <p>
 * Why: undo/redo is O(1) stack ops; the command stores captured piece,
 * castling, en passant, and prior hash so the board can roll back exactly.
 */
public final class MoveCommand {
    private final int startX;
    private final int startY;
    private final int endX;
    private final int endY;
    private final Piece piece;
    private final Piece captured;
    private final int capturedX;
    private final int capturedY;
    private final boolean castle;
    private final boolean enPassant;
    private final boolean promotion;
    private final Position prevEnPassant;
    private final boolean prevWhiteCastleK;
    private final boolean prevWhiteCastleQ;
    private final boolean prevBlackCastleK;
    private final boolean prevBlackCastleQ;
    private final Position prevWhiteKing;
    private final Position prevBlackKing;
    private final long prevHash;

    public MoveCommand(int startX, int startY, int endX, int endY, Piece piece,
                       Piece captured, int capturedX, int capturedY,
                       boolean castle, boolean enPassant, boolean promotion,
                       Position prevEnPassant,
                       boolean prevWhiteCastleK, boolean prevWhiteCastleQ,
                       boolean prevBlackCastleK, boolean prevBlackCastleQ,
                       Position prevWhiteKing, Position prevBlackKing, long prevHash) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
        this.piece = piece;
        this.captured = captured;
        this.capturedX = capturedX;
        this.capturedY = capturedY;
        this.castle = castle;
        this.enPassant = enPassant;
        this.promotion = promotion;
        this.prevEnPassant = prevEnPassant;
        this.prevWhiteCastleK = prevWhiteCastleK;
        this.prevWhiteCastleQ = prevWhiteCastleQ;
        this.prevBlackCastleK = prevBlackCastleK;
        this.prevBlackCastleQ = prevBlackCastleQ;
        this.prevWhiteKing = prevWhiteKing;
        this.prevBlackKing = prevBlackKing;
        this.prevHash = prevHash;
    }

    public void execute(Board board) {
        board.execute(this);
    }

    public void undo(Board board) {
        board.undo(this);
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public int getEndX() {
        return endX;
    }

    public int getEndY() {
        return endY;
    }

    public Piece getPiece() {
        return piece;
    }

    public Piece getCaptured() {
        return captured;
    }

    public int getCapturedX() {
        return capturedX;
    }

    public int getCapturedY() {
        return capturedY;
    }

    public boolean isCastle() {
        return castle;
    }

    public boolean isEnPassant() {
        return enPassant;
    }

    public boolean isPromotion() {
        return promotion;
    }

    public boolean isPawnOrCapture() {
        return piece.getType() == com.chess.lld.piece.PieceType.PAWN || captured != null;
    }

    public Position getPrevEnPassant() {
        return prevEnPassant;
    }

    public boolean getPrevWhiteCastleK() {
        return prevWhiteCastleK;
    }

    public boolean getPrevWhiteCastleQ() {
        return prevWhiteCastleQ;
    }

    public boolean getPrevBlackCastleK() {
        return prevBlackCastleK;
    }

    public boolean getPrevBlackCastleQ() {
        return prevBlackCastleQ;
    }

    public Position getPrevWhiteKing() {
        return prevWhiteKing;
    }

    public Position getPrevBlackKing() {
        return prevBlackKing;
    }

    public long getPrevHash() {
        return prevHash;
    }

    public int[] asCoords() {
        return new int[]{startX, startY, endX, endY};
    }

    @Override
    public String toString() {
        return piece.symbol() + " " + new Position(startX, startY) + "->" + new Position(endX, endY);
    }
}
