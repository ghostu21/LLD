package com.chess.lld.game;

import com.chess.lld.board.Board;
import com.chess.lld.board.ZobristHash;
import com.chess.lld.move.CommandHistory;
import com.chess.lld.move.MoveCommand;
import com.chess.lld.piece.Color;
import com.chess.lld.piece.Piece;
import com.chess.lld.player.GameObserver;
import com.chess.lld.player.Player;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * One isolated chess match: single-writer lock, clocks, history, observers.
 * <p>
 * Why: games scale horizontally by isolating state per game — do not
 * synchronize a global board. Move execution is atomic under {@code gameLock}.
 */
public final class Game {
    private final String gameId;
    private final Board board;
    private final CommandHistory history = new CommandHistory();
    private final ReentrantLock gameLock = new ReentrantLock();
    private final List<GameObserver> observers = new CopyOnWriteArrayList<>();
    private final Map<Long, Integer> repetitionCount = new HashMap<>();
    private final PlayerClock whiteClock;
    private final PlayerClock blackClock;
    private final long moveTimeMillis;
    private final ScheduledExecutorService scheduler;
    private Color currentTurn = Color.WHITE;
    private GameStatus status = GameStatus.IN_PROGRESS;
    private Color winner;
    private int fiftyMoveHalfPlies;
    private final Deque<Integer> fiftyHistory = new ArrayDeque<>();
    private int turnGeneration;
    private long turnStartedAtMillis;
    private ScheduledFuture<?> deadline;
    private Player whitePlayer;
    private Player blackPlayer;

    public Game(String gameId) {
        this(gameId, Board.standard(), Color.WHITE, 0L);
    }

    public Game(String gameId, long moveTimeMillis) {
        this(gameId, Board.standard(), Color.WHITE, moveTimeMillis);
    }

    public Game(String gameId, Board board, Color currentTurn, long moveTimeMillis) {
        this.gameId = gameId;
        this.board = board;
        this.currentTurn = currentTurn;
        this.moveTimeMillis = moveTimeMillis;
        this.whiteClock = new PlayerClock(moveTimeMillis);
        this.blackClock = new PlayerClock(moveTimeMillis);
        this.scheduler = moveTimeMillis > 0
                ? Executors.newSingleThreadScheduledExecutor(r -> {
                    Thread t = new Thread(r, "chess-clock-" + gameId);
                    t.setDaemon(true);
                    return t;
                })
                : null;
        bumpRepetition();
        if (moveTimeMillis > 0) {
            armDeadline();
        }
        checkGameState();
    }

    public Player getWhitePlayer() {
        return whitePlayer;
    }

    public Player getBlackPlayer() {
        return blackPlayer;
    }

    public String getGameId() {
        return gameId;
    }

    public Board getBoard() {
        return board;
    }

    public Color getCurrentTurn() {
        return currentTurn;
    }

    public GameStatus getStatus() {
        return status;
    }

    public Color getWinner() {
        return winner;
    }

    public CommandHistory getHistory() {
        return history;
    }

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void seat(Player player, Color color) {
        player.setColor(color);
        if (color == Color.WHITE) {
            whitePlayer = player;
        } else {
            blackPlayer = player;
        }
        addObserver(player);
    }

    /**
     * Applies a move under the per-game lock: validate, mutate, evaluate.
     *
     * @return true if the move was legal and applied
     */
    public boolean makeMove(int startX, int startY, int endX, int endY) {
        return makeMove(null, startX, startY, endX, endY);
    }

    public boolean makeMove(String playerId, int startX, int startY, int endX, int endY) {
        String notify = null;
        gameLock.lock();
        try {
            if (status != GameStatus.IN_PROGRESS) {
                return false;
            }
            if (!validateTurn(playerId)) {
                return false;
            }
            MoveCommand move = validateMove(startX, startY, endX, endY);
            if (move == null) {
                return false;
            }
            applyMove(move);
            checkGameState();
            notify = status == GameStatus.IN_PROGRESS
                    ? "MOVE " + move
                    : status.name() + (winner != null ? " winner=" + winner : "");
            return true;
        } finally {
            gameLock.unlock();
            if (notify != null) {
                publish(notify);
            }
        }
    }

    public boolean undoMove() {
        gameLock.lock();
        try {
            if (!history.canUndo() || status != GameStatus.IN_PROGRESS) {
                return false;
            }
            cancelDeadline();
            MoveCommand last = history.popUndo();
            decrementRepetition();
            last.undo(board);
            history.pushRedo(last);
            fiftyMoveHalfPlies = fiftyHistory.isEmpty() ? 0 : fiftyHistory.pop();
            currentTurn = currentTurn.opponent();
            turnGeneration++;
            armDeadline();
            return true;
        } finally {
            gameLock.unlock();
        }
    }

    public boolean redoMove() {
        gameLock.lock();
        try {
            if (!history.canRedo() || status != GameStatus.IN_PROGRESS) {
                return false;
            }
            MoveCommand next = history.popRedo();
            next.execute(board);
            history.pushUndoWithoutClearingRedo(next);
            currentTurn = currentTurn.opponent();
            fiftyHistory.push(fiftyMoveHalfPlies);
            if (next.isPawnOrCapture()) {
                fiftyMoveHalfPlies = 0;
            } else {
                fiftyMoveHalfPlies++;
            }
            bumpRepetition();
            turnGeneration++;
            armDeadline();
            return true;
        } finally {
            gameLock.unlock();
        }
    }

    public void resign(Color color) {
        gameLock.lock();
        try {
            if (status != GameStatus.IN_PROGRESS) {
                return;
            }
            status = GameStatus.RESIGNED;
            winner = color.opponent();
            cancelDeadline();
        } finally {
            gameLock.unlock();
        }
    }

    public void close() {
        cancelDeadline();
        if (scheduler != null) {
            scheduler.shutdownNow();
        }
    }

    public boolean isKingInCheck(Color color) {
        return board.isKingInCheck(color);
    }

    private boolean validateTurn(String playerId) {
        if (playerId == null) {
            return true;
        }
        Player seated = currentTurn == Color.WHITE ? whitePlayer : blackPlayer;
        return seated != null && playerId.equals(seated.getPlayerId());
    }

    private MoveCommand validateMove(int startX, int startY, int endX, int endY) {
        Piece piece = board.getPiece(startX, startY);
        if (piece == null || piece.getColor() != currentTurn) {
            return falseMove();
        }
        MoveCommand move = board.analyzeMove(startX, startY, endX, endY);
        if (move == null) {
            return null;
        }
        move.execute(board);
        boolean selfCheck = board.isKingInCheck(currentTurn);
        move.undo(board);
        return selfCheck ? null : move;
    }

    private MoveCommand falseMove() {
        return null;
    }

    private void applyMove(MoveCommand move) {
        cancelDeadline();
        deductClock();
        move.execute(board);
        history.push(move);
        fiftyHistory.push(fiftyMoveHalfPlies);
        if (move.isPawnOrCapture()) {
            fiftyMoveHalfPlies = 0;
        } else {
            fiftyMoveHalfPlies++;
        }
        currentTurn = currentTurn.opponent();
        bumpRepetition();
        turnGeneration++;
        if (status == GameStatus.IN_PROGRESS && moveTimeMillis > 0) {
            armDeadline();
        }
    }

    /**
     * Checkmate / stalemate / draw rules — legality is about the future, not just the move.
     */
    private void checkGameState() {
        Color toMove = currentTurn;
        boolean inCheck = board.isKingInCheck(toMove);
        boolean noMoves = noLegalMoves(toMove);
        if (inCheck && noMoves) {
            status = GameStatus.CHECKMATE;
            winner = toMove.opponent();
            cancelDeadline();
            return;
        }
        if (!inCheck && noMoves) {
            status = GameStatus.STALEMATE;
            cancelDeadline();
            return;
        }
        if (board.isInsufficientMaterial()) {
            status = GameStatus.DRAW_INSUFFICIENT;
            cancelDeadline();
            return;
        }
        if (fiftyMoveHalfPlies >= 100) {
            status = GameStatus.DRAW_FIFTY;
            cancelDeadline();
            return;
        }
        if (currentRepetition() >= 3) {
            status = GameStatus.DRAW_REPETITION;
            cancelDeadline();
        }
    }

    private boolean noLegalMoves(Color color) {
        for (int[] from : board.occupiedSquares(color)) {
            for (int x = 0; x < Board.SIZE; x++) {
                for (int y = 0; y < Board.SIZE; y++) {
                    Color savedTurn = currentTurn;
                    currentTurn = color;
                    MoveCommand candidate = validateMove(from[0], from[1], x, y);
                    currentTurn = savedTurn;
                    if (candidate != null) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private long positionKey() {
        long key = board.getBoardHash();
        if (currentTurn == Color.BLACK) {
            key ^= ZobristHash.BLACK_TO_MOVE;
        }
        return key;
    }

    private void bumpRepetition() {
        long key = positionKey();
        repetitionCount.merge(key, 1, Integer::sum);
    }

    private void decrementRepetition() {
        long key = positionKey();
        repetitionCount.computeIfPresent(key, (k, v) -> v <= 1 ? null : v - 1);
    }

    private int currentRepetition() {
        return repetitionCount.getOrDefault(positionKey(), 0);
    }

    private void deductClock() {
        if (moveTimeMillis <= 0) {
            return;
        }
        long elapsed = System.currentTimeMillis() - turnStartedAtMillis;
        clockFor(currentTurn).deduct(elapsed);
    }

    private PlayerClock clockFor(Color color) {
        return color == Color.WHITE ? whiteClock : blackClock;
    }

    private void armDeadline() {
        if (scheduler == null || status != GameStatus.IN_PROGRESS) {
            return;
        }
        cancelDeadline();
        turnStartedAtMillis = System.currentTimeMillis();
        int gen = turnGeneration;
        Color timed = currentTurn;
        long remaining = clockFor(timed).getRemainingTimeMillis();
        deadline = scheduler.schedule(() -> onTimeout(gen, timed), remaining, TimeUnit.MILLISECONDS);
    }

    private void onTimeout(int generation, Color timed) {
        gameLock.lock();
        try {
            if (status != GameStatus.IN_PROGRESS || turnGeneration != generation) {
                return;
            }
            status = GameStatus.TIMEOUT;
            winner = timed.opponent();
        } finally {
            gameLock.unlock();
            publish("TIMEOUT " + timed + " loses");
        }
    }

    private void cancelDeadline() {
        if (deadline != null) {
            deadline.cancel(false);
            deadline = null;
        }
    }

    private void publish(String message) {
        for (GameObserver observer : observers) {
            observer.update(gameId, message);
        }
    }

    public List<int[]> moveCoords() {
        List<int[]> coords = new ArrayList<>();
        for (MoveCommand move : history.movesInOrder()) {
            coords.add(move.asCoords());
        }
        return coords;
    }
}
