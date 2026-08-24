package com.lld.patterns.singleton.dcl;

/**
 * Double-checked locking without {@code volatile}. Widely shown; broken under JMM
 * (instruction reordering / stale cache). See {@link DBConnectionDoubleCheckedLockFix}.
 */
public class DBConnectionDoubleLocking {
    private static DBConnectionDoubleLocking instance = null;

    private DBConnectionDoubleLocking() {
    }

    public static DBConnectionDoubleLocking getInstance() {
        if (instance == null) {
            synchronized (DBConnectionDoubleLocking.class) {
                if (instance == null) {
                    instance = new DBConnectionDoubleLocking();
                }
            }
        }
        return instance;
    }

    public void displayMessage() {
        System.out.println("Double Locking Singleton - " + this);
    }
}
