package com.lld.patterns.objectpool.db;

import java.util.ArrayList;
import java.util.List;

/**
 * Object pool + singleton. Borrow from free list, return to free list.
 * Caps total connections at {@link #MAX_POOL_SIZE}.
 */
public class DBConnectionPoolManager {
    private static volatile DBConnectionPoolManager dbConnectionPoolManagerInstance = null;

    private final List<DBConnection> freeConnections = new ArrayList<>();
    private final List<DBConnection> inUseConnections = new ArrayList<>();
    private final int INITIAL_POOL_SIZE = 3;
    private final int MAX_POOL_SIZE = 6;

    private DBConnectionPoolManager() {
        for (int i = 0; i < INITIAL_POOL_SIZE; i++) {
            freeConnections.add(new DBConnection());
        }
    }

    public static DBConnectionPoolManager getInstance() {
        if (dbConnectionPoolManagerInstance == null) {
            synchronized (DBConnectionPoolManager.class) {
                if (dbConnectionPoolManagerInstance == null) {
                    dbConnectionPoolManagerInstance = new DBConnectionPoolManager();
                }
            }
        }
        return dbConnectionPoolManagerInstance;
    }

    public synchronized DBConnection getDBConnection() {
        if (freeConnections.isEmpty() && inUseConnections.size() < MAX_POOL_SIZE) {
            freeConnections.add(new DBConnection());
            System.out.println("New DBConnection created and added to freeConnections list.");
            System.out.println("freeConnections size: " + freeConnections.size());
            System.out.println("inUseConnections size: " + inUseConnections.size());
        } else if (freeConnections.isEmpty() && inUseConnections.size() >= MAX_POOL_SIZE) {
            System.out.println("Pool is full. Cannot create new DBConnection.");
            return null;
        }
        DBConnection dbConnection = freeConnections.remove(freeConnections.size() - 1);
        inUseConnections.add(dbConnection);
        System.out.println("DBConnection retrieved from freeConnections list and added to inUseConnections list.");
        System.out.println("freeConnections size: " + freeConnections.size());
        System.out.println("inUseConnections size: " + inUseConnections.size());
        return dbConnection;
    }

    public synchronized void releaseDBConnection(DBConnection dbConnection) {
        if (dbConnection != null) {
            inUseConnections.remove(dbConnection);
            freeConnections.add(dbConnection);
            System.out.println("DBConnection released from inUseConnections list and added to freeConnections list.");
            System.out.println("freeConnections size: " + freeConnections.size());
            System.out.println("inUseConnections size: " + inUseConnections.size());
        } else {
            System.out.println("DBConnection is null. Cannot release.");
        }
    }
}
