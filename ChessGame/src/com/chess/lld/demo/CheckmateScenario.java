package com.chess.lld.demo;

import com.chess.lld.game.Game;
import com.chess.lld.game.GameStatus;
import com.chess.lld.manager.GameManager;

/**
 * Fool's mate: f3, e5, g4, Qh4#.
 */
public final class CheckmateScenario implements FeatureScenario {
    @Override
    public void run() {
        System.out.println("--- Checkmate (fool's mate) ---");
        Game game = ChessGameService.newGame();
        game.makeMove(5, 1, 5, 2);
        game.makeMove(4, 6, 4, 4);
        game.makeMove(6, 1, 6, 3);
        boolean mateMove = game.makeMove(3, 7, 7, 3);
        System.out.println("Qh4 applied: " + mateMove);
        System.out.println("Status: " + game.getStatus());
        System.out.println("Winner: " + game.getWinner());
        System.out.println("Checkmate: " + (game.getStatus() == GameStatus.CHECKMATE));
        System.out.println(game.getBoard().toAscii());
        GameManager.getInstance().terminateGame(game.getGameId());
    }
}
