package com.chess.lld.demo;

import com.chess.lld.game.Game;
import com.chess.lld.manager.GameManager;
import com.chess.lld.manager.PlayerManager;

/**
 * Players subscribe as observers and receive move notifications.
 */
public final class ObserverScenario implements FeatureScenario {
    @Override
    public void run() {
        System.out.println("--- Observer notifications ---");
        Game game = ChessGameService.newGame();
        PlayerManager pm = PlayerManager.getInstance();
        pm.assignPlayerToGame("alice", game.getGameId());
        pm.assignPlayerToGame("bob", game.getGameId());
        game.makeMove("alice", 4, 1, 4, 3);
        GameManager.getInstance().terminateGame(game.getGameId());
    }
}
