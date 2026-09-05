package com.chess.lld.board;

import com.chess.lld.move.MoveCommand;
import com.chess.lld.piece.Color;
import com.chess.lld.piece.Piece;
import com.chess.lld.piece.PieceFactory;
import com.chess.lld.piece.PieceType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 8×8 position with incremental hash, attack maps, and piece counts.
 * <p>
 * Why: check is O(1) via attack sets; threefold uses Zobrist; insufficient
 * material uses counts — never rescan the whole board for those questions.
 */
public final class Board {
    public static final int SIZE = 8;

    private final Piece[][] grid = new Piece[SIZE][SIZE];
    private Position whiteKing;
    private Position blackKing;
    private Position enPassantTarget;
    private boolean whiteCastleK = true;
    private boolean whiteCastleQ = true;
    private boolean blackCastleK = true;
    private boolean blackCastleQ = true;
    private long boardHash;
    private final Set<Position> whiteAttacks = new HashSet<>();
    private final Set<Position> blackAttacks = new HashSet<>();
    private final Map<PieceType, Integer> whiteCount = new EnumMap<>(PieceType.class);
    private final Map<PieceType, Integer> blackCount = new EnumMap<>(PieceType.class);

    private Board() {
        for (PieceType type : PieceType.values()) {
            whiteCount.put(type, 0);
            blackCount.put(type, 0);
        }
    }

    public static Board standard() {
        Board board = new Board();
        PieceType[] back = {
                PieceType.ROOK, PieceType.KNIGHT, PieceType.BISHOP, PieceType.QUEEN,
                PieceType.KING, PieceType.BISHOP, PieceType.KNIGHT, PieceType.ROOK
        };
        for (int x = 0; x < SIZE; x++) {
            board.put(x, 0, PieceFactory.create(back[x], Color.WHITE));
            board.put(x, 1, PieceFactory.create(PieceType.PAWN, Color.WHITE));
            board.put(x, 6, PieceFactory.create(PieceType.PAWN, Color.BLACK));
            board.put(x, 7, PieceFactory.create(back[x], Color.BLACK));
        }
        board.recomputeHash();
        board.refreshAttackMaps();
        return board;
    }

    /**
     * Empty board for constructed endgame demos (stalemate, insufficient material).
     */
    public static Board empty() {
        Board board = new Board();
        board.whiteCastleK = board.whiteCastleQ = board.blackCastleK = board.blackCastleQ = false;
        board.recomputeHash();
        board.refreshAttackMaps();
        return board;
    }

    public void place(int x, int y, Piece piece) {
        put(x, y, piece);
        recomputeHash();
        refreshAttackMaps();
    }

    public boolean inBounds(int x, int y) {
        return x >= 0 && x < SIZE && y >= 0 && y < SIZE;
    }

    public Piece getPiece(int x, int y) {
        return inBounds(x, y) ? grid[x][y] : null;
    }

    public boolean isEmpty(int x, int y) {
        return inBounds(x, y) && grid[x][y] == null;
    }

    public Position getEnPassantTarget() {
        return enPassantTarget;
    }

    public long getBoardHash() {
        return boardHash;
    }

    public boolean canCastleKingSide(Color color) {
        return color == Color.WHITE ? whiteCastleK : blackCastleK;
    }

    public boolean canCastleQueenSide(Color color) {
        return color == Color.WHITE ? whiteCastleQ : blackCastleQ;
    }

    public Position kingPosition(Color color) {
        return color == Color.WHITE ? whiteKing : blackKing;
    }

    /**
     * O(1) check query against the opponent attack set.
     */
    public boolean isKingInCheck(Color color) {
        Position king = kingPosition(color);
        if (king == null) {
            return false;
        }
        return opponentAttacks(color).contains(king);
    }

    public boolean isSquareAttacked(int x, int y, Color byColor) {
        return attacksOf(byColor).contains(new Position(x, y));
    }

    public Set<Position> attacksOf(Color color) {
        return color == Color.WHITE ? whiteAttacks : blackAttacks;
    }

    public Set<Position> opponentAttacks(Color color) {
        return attacksOf(color.opponent());
    }

    public int pieceCount(Color color, PieceType type) {
        return color == Color.WHITE ? whiteCount.get(type) : blackCount.get(type);
    }

    public int totalPieces(Color color) {
        int sum = 0;
        Map<PieceType, Integer> counts = color == Color.WHITE ? whiteCount : blackCount;
        for (int n : counts.values()) {
            sum += n;
        }
        return sum;
    }

    /**
     * Destination is empty or occupied by the opponent (not own piece).
     */
    public boolean isCapturableOrEmpty(int startX, int startY, int endX, int endY) {
        if (!inBounds(endX, endY)) {
            return false;
        }
        Piece mover = getPiece(startX, startY);
        Piece dest = getPiece(endX, endY);
        return dest == null || (mover != null && dest.getColor() != mover.getColor());
    }

    /**
     * Squares strictly between start and end are empty (does not include ends).
     */
    public boolean isPathClear(int startX, int startY, int endX, int endY) {
        int dx = Integer.compare(endX, startX);
        int dy = Integer.compare(endY, startY);
        int x = startX + dx;
        int y = startY + dy;
        while (x != endX || y != endY) {
            if (!isEmpty(x, y)) {
                return false;
            }
            x += dx;
            y += dy;
        }
        return true;
    }

    public List<int[]> occupiedSquares(Color color) {
        List<int[]> squares = new ArrayList<>();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                Piece piece = grid[x][y];
                if (piece != null && piece.getColor() == color) {
                    squares.add(new int[]{x, y});
                }
            }
        }
        return squares;
    }

    /**
     * Builds an executable command (special moves included) or null if geometry fails.
     */
    public MoveCommand analyzeMove(int startX, int startY, int endX, int endY) {
        Piece piece = getPiece(startX, startY);
        if (piece == null || !piece.isValidMove(startX, startY, endX, endY, this)) {
            return null;
        }
        boolean castle = piece.getType() == PieceType.KING && Math.abs(endX - startX) == 2;
        boolean promotion = piece.getType() == PieceType.PAWN && (endY == 0 || endY == 7);
        Position ep = enPassantTarget;
        boolean enPassant = piece.getType() == PieceType.PAWN
                && ep != null && ep.getX() == endX && ep.getY() == endY && getPiece(endX, endY) == null;
        int capturedX = endX;
        int capturedY = endY;
        if (enPassant) {
            capturedY = startY;
        }
        Piece captured = castle ? null : getPiece(capturedX, capturedY);
        return new MoveCommand(
                startX, startY, endX, endY, piece, captured, capturedX, capturedY,
                castle, enPassant, promotion,
                enPassantTarget, whiteCastleK, whiteCastleQ, blackCastleK, blackCastleQ,
                whiteKing, blackKing, boardHash);
    }

    public void execute(MoveCommand move) {
        apply(move, false);
    }

    public void undo(MoveCommand move) {
        apply(move, true);
    }

    private void apply(MoveCommand move, boolean reverse) {
        if (reverse) {
            if (move.isCastle()) {
                int rookFrom = move.getEndX() == 6 ? 5 : 3;
                int rookTo = move.getEndX() == 6 ? 7 : 0;
                Piece rook = getPiece(rookFrom, move.getStartY());
                rawClear(rookFrom, move.getStartY());
                rawPut(rookTo, move.getStartY(), rook);
            }
            if (move.isPromotion()) {
                rawClear(move.getEndX(), move.getEndY());
                rawPut(move.getStartX(), move.getStartY(), move.getPiece());
            } else {
                rawClear(move.getEndX(), move.getEndY());
                rawPut(move.getStartX(), move.getStartY(), move.getPiece());
            }
            if (move.getCaptured() != null) {
                rawPut(move.getCapturedX(), move.getCapturedY(), move.getCaptured());
            }
            whiteCastleK = move.getPrevWhiteCastleK();
            whiteCastleQ = move.getPrevWhiteCastleQ();
            blackCastleK = move.getPrevBlackCastleK();
            blackCastleQ = move.getPrevBlackCastleQ();
            enPassantTarget = move.getPrevEnPassant();
            whiteKing = move.getPrevWhiteKing();
            blackKing = move.getPrevBlackKing();
            boardHash = move.getPrevHash();
            refreshAttackMaps();
            return;
        }

        if (move.getCaptured() != null) {
            rawClear(move.getCapturedX(), move.getCapturedY());
        }
        rawClear(move.getStartX(), move.getStartY());
        Piece landing = move.getPiece();
        if (move.isPromotion()) {
            landing = PieceFactory.create(PieceType.QUEEN, move.getPiece().getColor());
        }
        rawPut(move.getEndX(), move.getEndY(), landing);
        if (move.isCastle()) {
            int rookFrom = move.getEndX() == 6 ? 7 : 0;
            int rookTo = move.getEndX() == 6 ? 5 : 3;
            Piece rook = getPiece(rookFrom, move.getStartY());
            rawClear(rookFrom, move.getStartY());
            rawPut(rookTo, move.getStartY(), rook);
        }
        updateCastlingRights(move);
        updateEnPassant(move);
        recomputeHash();
        refreshAttackMaps();
    }

    /**
     * King vs king, king+bishop vs king, king+knight vs king.
     */
    public boolean isInsufficientMaterial() {
        int white = totalPieces(Color.WHITE);
        int black = totalPieces(Color.BLACK);
        if (white == 1 && black == 1) {
            return true;
        }
        if (white + black == 3) {
            Color extra = white == 2 ? Color.WHITE : Color.BLACK;
            return pieceCount(extra, PieceType.BISHOP) == 1
                    || pieceCount(extra, PieceType.KNIGHT) == 1;
        }
        return false;
    }

    public String toAscii() {
        StringBuilder sb = new StringBuilder();
        for (int y = SIZE - 1; y >= 0; y--) {
            sb.append(y + 1).append(' ');
            for (int x = 0; x < SIZE; x++) {
                Piece piece = grid[x][y];
                sb.append(piece == null ? '.' : piece.symbol()).append(' ');
            }
            sb.append('\n');
        }
        sb.append("  a b c d e f g h");
        return sb.toString();
    }

    private void updateEnPassant(MoveCommand move) {
        Piece piece = move.getPiece();
        if (piece.getType() == PieceType.PAWN && Math.abs(move.getEndY() - move.getStartY()) == 2) {
            enPassantTarget = new Position(move.getStartX(), (move.getStartY() + move.getEndY()) / 2);
        } else {
            enPassantTarget = null;
        }
    }

    private void updateCastlingRights(MoveCommand move) {
        Piece piece = move.getPiece();
        if (piece.getType() == PieceType.KING) {
            if (piece.getColor() == Color.WHITE) {
                whiteCastleK = whiteCastleQ = false;
            } else {
                blackCastleK = blackCastleQ = false;
            }
        }
        if (piece.getType() == PieceType.ROOK) {
            revokeRookRights(move.getStartX(), move.getStartY(), piece.getColor());
        }
        Piece captured = move.getCaptured();
        if (captured != null && captured.getType() == PieceType.ROOK) {
            revokeRookRights(move.getCapturedX(), move.getCapturedY(), captured.getColor());
        }
    }

    private void revokeRookRights(int x, int y, Color color) {
        if (color == Color.WHITE && y == 0) {
            if (x == 7) {
                whiteCastleK = false;
            }
            if (x == 0) {
                whiteCastleQ = false;
            }
        }
        if (color == Color.BLACK && y == 7) {
            if (x == 7) {
                blackCastleK = false;
            }
            if (x == 0) {
                blackCastleQ = false;
            }
        }
    }

    private void put(int x, int y, Piece piece) {
        rawPut(x, y, piece);
    }

    private void rawPut(int x, int y, Piece piece) {
        Piece previous = grid[x][y];
        if (previous != null) {
            bumpCount(previous, -1);
        }
        grid[x][y] = piece;
        if (piece != null) {
            bumpCount(piece, 1);
            if (piece.getType() == PieceType.KING) {
                Position pos = new Position(x, y);
                if (piece.getColor() == Color.WHITE) {
                    whiteKing = pos;
                } else {
                    blackKing = pos;
                }
            }
        }
    }

    private void rawClear(int x, int y) {
        Piece previous = grid[x][y];
        if (previous != null) {
            bumpCount(previous, -1);
        }
        grid[x][y] = null;
    }

    private void bumpCount(Piece piece, int delta) {
        Map<PieceType, Integer> counts = piece.getColor() == Color.WHITE ? whiteCount : blackCount;
        counts.put(piece.getType(), counts.get(piece.getType()) + delta);
    }

    private void recomputeHash() {
        long hash = 0L;
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                Piece piece = grid[x][y];
                if (piece != null) {
                    hash ^= ZobristHash.pieceKey(piece, x, y);
                }
            }
        }
        hash ^= ZobristHash.castlingKey(whiteCastleK, whiteCastleQ, blackCastleK, blackCastleQ);
        hash ^= ZobristHash.enPassantKey(enPassantTarget);
        boardHash = hash;
    }

    public void refreshAttackMaps() {
        whiteAttacks.clear();
        blackAttacks.clear();
        for (int x = 0; x < SIZE; x++) {
            for (int y = 0; y < SIZE; y++) {
                Piece piece = grid[x][y];
                if (piece != null) {
                    addAttacks(piece, x, y, attacksOf(piece.getColor()));
                }
            }
        }
    }

    private void addAttacks(Piece piece, int x, int y, Set<Position> attacks) {
        switch (piece.getType()) {
            case PAWN -> {
                int dir = piece.getColor() == Color.WHITE ? 1 : -1;
                addIfInBounds(attacks, x - 1, y + dir);
                addIfInBounds(attacks, x + 1, y + dir);
            }
            case KNIGHT -> {
                int[][] d = {{1, 2}, {2, 1}, {-1, 2}, {-2, 1}, {1, -2}, {2, -1}, {-1, -2}, {-2, -1}};
                for (int[] step : d) {
                    addIfInBounds(attacks, x + step[0], y + step[1]);
                }
            }
            case KING -> {
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = -1; dy <= 1; dy++) {
                        if (dx != 0 || dy != 0) {
                            addIfInBounds(attacks, x + dx, y + dy);
                        }
                    }
                }
            }
            case ROOK -> addRays(attacks, x, y, new int[][]{{1, 0}, {-1, 0}, {0, 1}, {0, -1}});
            case BISHOP -> addRays(attacks, x, y, new int[][]{{1, 1}, {1, -1}, {-1, 1}, {-1, -1}});
            case QUEEN -> addRays(attacks, x, y, new int[][]{
                    {1, 0}, {-1, 0}, {0, 1}, {0, -1}, {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
            });
        }
    }

    private void addRays(Set<Position> attacks, int x, int y, int[][] dirs) {
        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];
            while (inBounds(nx, ny)) {
                attacks.add(new Position(nx, ny));
                if (grid[nx][ny] != null) {
                    break;
                }
                nx += dir[0];
                ny += dir[1];
            }
        }
    }

    private void addIfInBounds(Set<Position> attacks, int x, int y) {
        if (inBounds(x, y)) {
            attacks.add(new Position(x, y));
        }
    }
}
