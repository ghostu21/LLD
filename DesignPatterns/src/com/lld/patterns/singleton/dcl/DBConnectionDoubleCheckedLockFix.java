package com.lld.patterns.singleton.dcl;

/**
 * Correct DCL: {@code volatile} on the instance. Happens-before so other threads
 * never see a half-built object (port still 0 / default).
 *
 * <p>The note's {@code getConnectionObj(int)} ignores the port after the first create.
 * Callers must not treat a later port argument as a reconfigure.
 */
public class DBConnectionDoubleCheckedLockFix {
    private static volatile DBConnectionDoubleCheckedLockFix connectionObj = null;
    private final int portNumber;

    private DBConnectionDoubleCheckedLockFix(int portNumberValue) {
        portNumber = portNumberValue;
    }

    public static DBConnectionDoubleCheckedLockFix getConnectionObj(int portNumberValue) {
        if (connectionObj == null) {
            synchronized (DBConnectionDoubleCheckedLockFix.class) {
                if (connectionObj == null) {
                    connectionObj = new DBConnectionDoubleCheckedLockFix(portNumberValue);
                }
            }
        }
        return connectionObj;
    }

    public int getPortNumber() {
        return portNumber;
    }

    public void displayMessage() {
        System.out.println("Singleton - Double Checked Locking - Fix - " + this
                + " port=" + portNumber);
    }
}
