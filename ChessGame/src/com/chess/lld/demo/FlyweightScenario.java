package com.chess.lld.demo;

import com.chess.lld.piece.Color;
import com.chess.lld.piece.Piece;
import com.chess.lld.piece.PieceFactory;
import com.chess.lld.piece.PieceType;

/**
 * All white pawns share one PieceProperties instance.
 */
public final class FlyweightScenario implements FeatureScenario {
    @Override
    public void run() {
        System.out.println("--- Flyweight piece properties ---");
        Piece a = PieceFactory.create(PieceType.PAWN, Color.WHITE);
        Piece b = PieceFactory.create(PieceType.PAWN, Color.WHITE);
        Piece c = PieceFactory.create(PieceType.PAWN, Color.BLACK);
        System.out.println("White pawn properties identical: " + (a.getProperties() == b.getProperties()));
        System.out.println("Black pawn is a different flyweight: " + (a.getProperties() != c.getProperties()));
        System.out.println("Cached (type,color) keys: " + PieceFactory.cachedPropertyCount());
    }
}
