package com.lld.patterns.objectpool.db;

/**
 * Reusable resource. Real JDBC is omitted so the demo runs without MySQL;
 * construction still stands in for an expensive open.
 */
public class DBConnection {
    private static int nextId = 1;
    private final int id;

    public DBConnection() {
        this.id = nextId++;
        System.out.println("Opened expensive DBConnection #" + id);
    }

    public int getId() {
        return id;
    }
}
