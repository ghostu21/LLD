package com.chess.lld.strategy;

import com.chess.lld.board.Board;

/**
 * Strategy for one piece type's movement geometry.
 * <p>
 * Why: adding a fairy piece (or variant) is a new strategy, not a Board
 * rewrite. King-safety is applied later in {@code Game} so strategies stay
 * piece-centric.
 */
public interface MoveStrategy {
    /**
     * @return true if the move matches this piece's rules on the current board
     *         (path, occupancy, special moves) — does not test self-check
     */
    boolean isValidMove(int startX, int startY, int endX, int endY, Board board);
}
