package com.lld.patterns.singleton.dcl;

/**
 * Broken DCL with a field ({@code portNumber}): {@code new} is not atomic.
 * Another thread can see a non-null reference before {@code portNumber} is written.
 */
public class DBConnectionDoubleCheckedLockIssue {
    private static DBConnectionDoubleCheckedLockIssue connectionObj = null;
    int portNumber;

    private DBConnectionDoubleCheckedLockIssue(int portNumberValue) {
        portNumber = portNumberValue;
    }

    public static DBConnectionDoubleCheckedLockIssue getConnectionObj() {
        if (connectionObj == null) {
            synchronized (DBConnectionDoubleCheckedLockIssue.class) {
                if (connectionObj == null) {
                    connectionObj = new DBConnectionDoubleCheckedLockIssue(5567);
                }
            }
        }
        return connectionObj;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void displayMessage() {
        System.out.println("Singleton - Double Checked Locking - Issue - " + this);
    }
}
