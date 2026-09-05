package com.chess.lld.board;

import com.chess.lld.piece.Color;
import com.chess.lld.piece.Piece;
import com.chess.lld.piece.PieceType;

import java.util.Random;

/**
 * Precomputed random 64-bit keys for incremental position hashing.
 * <p>
 * Why: threefold repetition must be O(1), not an 8×8 board compare.
 * Interview line: “Game logs are for humans. Hashes are for engines.”
 */
public final class ZobristHash {
    private static final long[][] PIECES = new long[12][64];
    public static final long BLACK_TO_MOVE;
    private static final long[] CASTLING = new long[4];
    private static final long[] EN_PASSANT_FILE = new long[8];

    static {
        Random random = new Random(20260828L);
        for (int p = 0; p < 12; p++) {
            for (int sq = 0; sq < 64; sq++) {
                PIECES[p][sq] = random.nextLong();
            }
        }
        BLACK_TO_MOVE = random.nextLong();
        for (int i = 0; i < 4; i++) {
            CASTLING[i] = random.nextLong();
        }
        for (int i = 0; i < 8; i++) {
            EN_PASSANT_FILE[i] = random.nextLong();
        }
    }

    private ZobristHash() {
    }

    public static int pieceIndex(PieceType type, Color color) {
        return type.ordinal() + (color == Color.WHITE ? 0 : 6);
    }

    public static long pieceKey(Piece piece, int x, int y) {
        return PIECES[pieceIndex(piece.getType(), piece.getColor())][y * 8 + x];
    }

    public static long castlingKey(boolean wk, boolean wq, boolean bk, boolean bq) {
        long h = 0L;
        if (wk) {
            h ^= CASTLING[0];
        }
        if (wq) {
            h ^= CASTLING[1];
        }
        if (bk) {
            h ^= CASTLING[2];
        }
        if (bq) {
            h ^= CASTLING[3];
        }
        return h;
    }

    public static long enPassantKey(Position ep) {
        return ep == null ? 0L : EN_PASSANT_FILE[ep.getX()];
    }
}
