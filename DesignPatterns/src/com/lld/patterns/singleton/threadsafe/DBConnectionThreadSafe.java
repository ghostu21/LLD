package com.lld.patterns.singleton.threadsafe;

/**
 * Lazy + {@code synchronized getInstance()}. Safe, but every call takes the lock.
 */
public class DBConnectionThreadSafe {
    private static DBConnectionThreadSafe instance = null;

    private DBConnectionThreadSafe() {
    }

    public static synchronized DBConnectionThreadSafe getInstance() {
        if (instance == null) {
            instance = new DBConnectionThreadSafe();
        }
        return instance;
    }

    public void displayMessage() {
        System.out.println("Thread Safe Singleton - " + this);
    }
}
