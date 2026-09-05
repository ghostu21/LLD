package com.chess.lld.demo;

import com.chess.lld.game.Game;
import com.chess.lld.game.GameStatus;
import com.chess.lld.manager.GameManager;

/**
 * Move deadline is a game rule: expiry auto-loses without a UI timer.
 */
public final class TimeoutScenario implements FeatureScenario {
    @Override
    public void run() throws Exception {
        System.out.println("--- Move clock / timeout ---");
        String id = GameManager.getInstance().createGame(80L);
        Game game = GameManager.getInstance().getGame(id);
        Thread.sleep(150L);
        System.out.println("Status after stall: " + game.getStatus());
        System.out.println("Timeout: " + (game.getStatus() == GameStatus.TIMEOUT));
        System.out.println("Winner: " + game.getWinner());
        GameManager.getInstance().terminateGame(id);
    }
}
