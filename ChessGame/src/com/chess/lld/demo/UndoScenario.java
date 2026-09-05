package com.chess.lld.demo;

import com.chess.lld.game.Game;
import com.chess.lld.manager.GameManager;

/**
 * Command + Deque undo/redo.
 */
public final class UndoScenario implements FeatureScenario {
    @Override
    public void run() {
        System.out.println("--- Undo / redo stacks ---");
        Game game = ChessGameService.newGame();
        game.makeMove(4, 1, 4, 3);
        System.out.println("After e2-e4, e4 occupied: " + (game.getBoard().getPiece(4, 3) != null));
        game.undoMove();
        System.out.println("After undo, e4 empty: " + (game.getBoard().getPiece(4, 3) == null));
        System.out.println("Turn back to WHITE: " + game.getCurrentTurn());
        game.redoMove();
        System.out.println("After redo, e4 occupied: " + (game.getBoard().getPiece(4, 3) != null));
        GameManager.getInstance().terminateGame(game.getGameId());
    }
}
