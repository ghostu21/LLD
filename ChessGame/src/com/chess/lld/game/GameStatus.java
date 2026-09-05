package com.chess.lld.game;

/**
 * Terminal and in-progress states after {@code checkGameState()}.
 */
public enum GameStatus {
    IN_PROGRESS,
    CHECKMATE,
    STALEMATE,
    DRAW_REPETITION,
    DRAW_FIFTY,
    DRAW_INSUFFICIENT,
    TIMEOUT,
    RESIGNED
}
