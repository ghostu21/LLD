package com.lld.patterns.objectpool.demo;

import com.lld.patterns.objectpool.db.DBConnection;
import com.lld.patterns.objectpool.db.DBConnectionPoolManager;

public class ObjectPoolPatternDemo {
    public static void main(String[] args) {
        System.out.println("======= Object Pool Design Pattern ======");
        DBConnectionPoolManager poolManager = DBConnectionPoolManager.getInstance();

        DBConnection dbConnection1 = poolManager.getDBConnection();
        DBConnection dbConnection2 = poolManager.getDBConnection();
        DBConnection dbConnection3 = poolManager.getDBConnection();
        DBConnection dbConnection4 = poolManager.getDBConnection();
        DBConnection dbConnection5 = poolManager.getDBConnection();
        DBConnection dbConnection6 = poolManager.getDBConnection();

        DBConnection nullDBConnection = poolManager.getDBConnection();
        System.out.println(nullDBConnection == null
                ? "DBConnection is null as POOL is full."
                : "DBConnection is not null");

        poolManager.releaseDBConnection(dbConnection6);
        DBConnection dbConnection7 = poolManager.getDBConnection();
        System.out.println("Reused released connection? " + (dbConnection7 == dbConnection6));

        DBConnectionPoolManager poolManager2 = DBConnectionPoolManager.getInstance();
        System.out.println("====== Same Instance? ======");
        System.out.println(poolManager == poolManager2
                ? "Same instance of DBConnectionPoolManager"
                : "Different instances of DBConnectionPoolManager");
    }
}
