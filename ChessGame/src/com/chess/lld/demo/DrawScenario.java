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
 * King vs king — insufficient material draw.
 */
public final class DrawScenario implements FeatureScenario {
    @Override
    public void run() {
        System.out.println("--- Insufficient material ---");
        Board board = Board.empty();
        board.place(0, 0, PieceFactory.create(PieceType.KING, Color.WHITE));
        board.place(7, 7, PieceFactory.create(PieceType.KING, Color.BLACK));
        Game game = new Game(UUID.randomUUID().toString(), board, Color.WHITE, 0L);
        GameManager.getInstance().createGame(game);
        System.out.println("Status: " + game.getStatus());
        System.out.println("Draw: " + (game.getStatus() == GameStatus.DRAW_INSUFFICIENT));
        GameManager.getInstance().terminateGame(game.getGameId());
    }
}
