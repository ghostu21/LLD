package com.chess.lld.demo;

import com.chess.lld.game.Game;
import com.chess.lld.manager.GameManager;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Two threads race the same ply; per-game lock allows only one writer.
 */
public final class ConcurrencyScenario implements FeatureScenario {
    @Override
    public void run() throws Exception {
        System.out.println("--- Per-game lock (single writer) ---");
        Game game = ChessGameService.newGame();
        AtomicInteger wins = new AtomicInteger();
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);

        Runnable attempt = () -> {
            try {
                start.await();
                if (game.makeMove(4, 1, 4, 3) || game.makeMove(3, 1, 3, 3)) {
                    wins.incrementAndGet();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        };
        new Thread(attempt, "t1").start();
        new Thread(attempt, "t2").start();
        start.countDown();
        done.await();
        System.out.println("Successful moves from 2 racers: " + wins.get() + " (expect 1)");
        System.out.println("Turn: " + game.getCurrentTurn());
        GameManager.getInstance().terminateGame(game.getGameId());
    }
}
