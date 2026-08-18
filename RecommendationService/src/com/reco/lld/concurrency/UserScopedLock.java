package com.reco.lld.concurrency;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Striped per-user lock so preference and interaction writes for the same
 * shopper are serialized without a global lock.
 * <p>
 * Why: concurrent hide + recommend + tag-update on one user must not
 * interleave "append event" and "bump generation" out of order.
 */
public final class UserScopedLock {
    private static final int STRIPES = 32;
    private final ReentrantLock[] stripes = new ReentrantLock[STRIPES];

    public UserScopedLock() {
        for (int i = 0; i < STRIPES; i++) {
            stripes[i] = new ReentrantLock();
        }
    }

    public void run(String userId, Runnable action) {
        if (userId == null) throw new IllegalArgumentException("userId required");
        ReentrantLock lock = stripes[Math.floorMod(userId.hashCode(), STRIPES)];
        lock.lock();
        try {
            action.run();
        } finally {
            lock.unlock();
        }
    }
}
