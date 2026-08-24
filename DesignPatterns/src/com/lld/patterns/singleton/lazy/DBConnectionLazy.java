package com.lld.patterns.singleton.lazy;

/**
 * Lazy: created on first {@code getInstance()}. Not thread-safe.
 */
public class DBConnectionLazy {
    private static DBConnectionLazy instance = null;

    private DBConnectionLazy() {
    }

    public static DBConnectionLazy getInstance() {
        if (instance == null) {
            instance = new DBConnectionLazy();
        }
        return instance;
    }

    public void displayMessage() {
        System.out.println("Lazy Initialization - Singleton - " + this);
    }
}
