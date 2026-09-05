package com.chess.lld.demo;

import com.chess.lld.game.Game;
import com.chess.lld.game.GameStatus;
import com.chess.lld.manager.GameManager;

/**
 * Knights out and back until the start position occurs three times.
 */
public final class RepetitionScenario implements FeatureScenario {
    @Override
    public void run() {
        System.out.println("--- Threefold repetition (Zobrist) ---");
        Game game = ChessGameService.newGame();
        int[][] cycle = {
                {1, 0, 2, 2}, {1, 7, 2, 5},
                {2, 2, 1, 0}, {2, 5, 1, 7}
        };
        for (int i = 0; i < 3 && game.getStatus() == GameStatus.IN_PROGRESS; i++) {
            for (int[] m : cycle) {
                game.makeMove(m[0], m[1], m[2], m[3]);
            }
        }
        System.out.println("Status: " + game.getStatus());
        System.out.println("Threefold: " + (game.getStatus() == GameStatus.DRAW_REPETITION));
        GameManager.getInstance().terminateGame(game.getGameId());
    }
}
