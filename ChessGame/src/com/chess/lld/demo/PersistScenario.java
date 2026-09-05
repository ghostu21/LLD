package com.chess.lld.demo;

import com.chess.lld.game.Game;
import com.chess.lld.manager.GameManager;
import com.chess.lld.persist.GameStore;

/**
 * Save move list and restore by replay.
 */
public final class PersistScenario implements FeatureScenario {
    @Override
    public void run() {
        System.out.println("--- Save / load ---");
        Game game = ChessGameService.newGame();
        game.makeMove(4, 1, 4, 3);
        game.makeMove(4, 6, 4, 4);
        GameStore.getInstance().save(game);
        Game restored = GameStore.getInstance().load(game.getGameId());
        System.out.println("Restored e4 occupied: " + (restored.getBoard().getPiece(4, 3) != null));
        System.out.println("Restored turn: " + restored.getCurrentTurn());
        GameManager.getInstance().terminateGame(game.getGameId());
        GameManager.getInstance().terminateGame(restored.getGameId());
    }
}
