package com.chess.lld.game;

/**
 * Per-player remaining time (server-side rule, not a UI widget).
 */
public final class PlayerClock {
    private long remainingTimeMillis;

    public PlayerClock(long remainingTimeMillis) {
        this.remainingTimeMillis = remainingTimeMillis;
    }

    public long getRemainingTimeMillis() {
        return remainingTimeMillis;
    }

    public void deduct(long elapsedMillis) {
        remainingTimeMillis = Math.max(0L, remainingTimeMillis - elapsedMillis);
    }
}
