package com.chess.lld.demo;

import com.chess.lld.game.Game;
import com.chess.lld.manager.GameManager;

/**
 * Standard pawn double-step e2-e4 (file e = 4, rank 2 = y 1).
 */
public final class MoveScenario implements FeatureScenario {
    @Override
    public void run() {
        System.out.println("--- Move validation (e2-e4) ---");
        Game game = ChessGameService.newGame();
        boolean ok = game.makeMove(4, 1, 4, 3);
        System.out.println("e2-e4 applied: " + ok);
        System.out.println("Turn now: " + game.getCurrentTurn());
        System.out.println(game.getBoard().toAscii());
        GameManager.getInstance().terminateGame(game.getGameId());
    }
}
