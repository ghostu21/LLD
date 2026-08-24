package com.lld.patterns.singleton.eager;

/**
 * Eager: instance created at class load. Thread-safe, may construct even if never used.
 */
public class DBConnectionEager {
    private static final DBConnectionEager instance = new DBConnectionEager();

    private DBConnectionEager() {
    }

    public static DBConnectionEager getInstance() {
        return instance;
    }

    public void displayMessage() {
        System.out.println("Eager Initialization - Singleton - " + this);
    }
}
