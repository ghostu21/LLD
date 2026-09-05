package com.chess.lld.piece;

import com.chess.lld.strategy.BishopMoveStrategy;
import com.chess.lld.strategy.KingMoveStrategy;
import com.chess.lld.strategy.KnightMoveStrategy;
import com.chess.lld.strategy.MoveStrategy;
import com.chess.lld.strategy.PawnMoveStrategy;
import com.chess.lld.strategy.QueenMoveStrategy;
import com.chess.lld.strategy.RookMoveStrategy;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory + Flyweight cache for pieces.
 * <p>
 * Why: Factory hides strategy wiring; Flyweight shares {@link PieceProperties}
 * so 1M concurrent games do not allocate 32M duplicate (type, color) objects.
 */
public final class PieceFactory {
    private static final Map<String, PieceProperties> PROPERTIES = new ConcurrentHashMap<>();
    private static final MoveStrategy PAWN = new PawnMoveStrategy();
    private static final MoveStrategy ROOK = new RookMoveStrategy();
    private static final MoveStrategy KNIGHT = new KnightMoveStrategy();
    private static final MoveStrategy BISHOP = new BishopMoveStrategy();
    private static final MoveStrategy QUEEN = new QueenMoveStrategy();
    private static final MoveStrategy KING = new KingMoveStrategy();

    private PieceFactory() {
    }

    /**
     * Returns the shared intrinsic state for (type, color).
     */
    public static PieceProperties getPieceProperties(PieceType type, Color color) {
        String key = type.name() + ":" + color.name();
        return PROPERTIES.computeIfAbsent(key, k -> new PieceProperties(type, color));
    }

    /**
     * Creates a board piece with shared properties and a stateless strategy.
     */
    public static Piece create(PieceType type, Color color) {
        return new Piece(getPieceProperties(type, color), strategyFor(type));
    }

    /**
     * Visible for the flyweight demo: how many unique property objects exist.
     */
    public static int cachedPropertyCount() {
        return PROPERTIES.size();
    }

    private static MoveStrategy strategyFor(PieceType type) {
        return switch (type) {
            case PAWN -> PAWN;
            case ROOK -> ROOK;
            case KNIGHT -> KNIGHT;
            case BISHOP -> BISHOP;
            case QUEEN -> QUEEN;
            case KING -> KING;
        };
    }
}
