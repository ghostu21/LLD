package com.chess.lld.demo;

import com.chess.lld.board.Board;
import com.chess.lld.game.Game;
import com.chess.lld.game.GameStatus;
import com.chess.lld.manager.GameManager;
import com.chess.lld.piece.Color;
import com.chess.lld.piece.PieceFactory;
import com.chess.lld.piece.PieceType;

import java.util.UUID;

/**
 * Black to move, Ka8 / Kc6 / Qc7 — no legal moves, not in check.
 */
public final class StalemateScenario implements FeatureScenario {
    @Override
    public void run() {
        System.out.println("--- Stalemate ---");
        Board board = Board.empty();
        board.place(0, 7, PieceFactory.create(PieceType.KING, Color.BLACK));
        board.place(2, 5, PieceFactory.create(PieceType.KING, Color.WHITE));
        board.place(2, 6, PieceFactory.create(PieceType.QUEEN, Color.WHITE));
        Game game = new Game(UUID.randomUUID().toString(), board, Color.BLACK, 0L);
        GameManager.getInstance().createGame(game);
        System.out.println("Status: " + game.getStatus());
        System.out.println("Stalemate: " + (game.getStatus() == GameStatus.STALEMATE));
        System.out.println(game.getBoard().toAscii());
        GameManager.getInstance().terminateGame(game.getGameId());
    }
}
