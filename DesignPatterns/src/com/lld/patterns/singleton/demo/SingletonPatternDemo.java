package com.lld.patterns.singleton.demo;

import com.lld.patterns.singleton.dcl.DBConnectionDoubleCheckedLockFix;
import com.lld.patterns.singleton.dcl.DBConnectionDoubleLocking;
import com.lld.patterns.singleton.eager.DBConnectionEager;
import com.lld.patterns.singleton.lazy.DBConnectionLazy;
import com.lld.patterns.singleton.threadsafe.DBConnectionThreadSafe;

public class SingletonPatternDemo {
    public static void main(String[] args) {
        testEager();
        testLazy();
        testThreadSafe();
        testDoubleLocking();
        testVolatileFix();
    }

    private static void testEager() {
        System.out.println("====== Testing Eager Initialization ======");
        DBConnectionEager eager1 = DBConnectionEager.getInstance();
        DBConnectionEager eager2 = DBConnectionEager.getInstance();
        eager1.displayMessage();
        eager2.displayMessage();
        System.out.println("Same instance? " + (eager1 == eager2));
        System.out.println();
    }

    private static void testLazy() {
        System.out.println("====== Testing Lazy Initialization ======");
        DBConnectionLazy lazy1 = DBConnectionLazy.getInstance();
        DBConnectionLazy lazy2 = DBConnectionLazy.getInstance();
        lazy1.displayMessage();
        lazy2.displayMessage();
        System.out.println("Same instance? " + (lazy1 == lazy2));
        System.out.println();
    }

    private static void testThreadSafe() {
        System.out.println("====== Testing Thread Safe ======");
        DBConnectionThreadSafe threadSafe1 = DBConnectionThreadSafe.getInstance();
        DBConnectionThreadSafe threadSafe2 = DBConnectionThreadSafe.getInstance();
        threadSafe1.displayMessage();
        threadSafe2.displayMessage();
        System.out.println("Same instance? " + (threadSafe1 == threadSafe2));
        System.out.println();
    }

    private static void testDoubleLocking() {
        System.out.println("====== Testing Double Locking ======");
        DBConnectionDoubleLocking doubleLocking1 = DBConnectionDoubleLocking.getInstance();
        DBConnectionDoubleLocking doubleLocking2 = DBConnectionDoubleLocking.getInstance();
        doubleLocking1.displayMessage();
        doubleLocking2.displayMessage();
        System.out.println("Same instance? " + (doubleLocking1 == doubleLocking2));
        System.out.println();
    }

    private static void testVolatileFix() {
        System.out.println("====== Testing Double Locking + volatile ======");
        DBConnectionDoubleCheckedLockFix a = DBConnectionDoubleCheckedLockFix.getConnectionObj(5567);
        DBConnectionDoubleCheckedLockFix b = DBConnectionDoubleCheckedLockFix.getConnectionObj(9999);
        a.displayMessage();
        b.displayMessage();
        System.out.println("Same instance? " + (a == b));
        System.out.println("Port stays first-wins (5567), not 9999: " + a.getPortNumber());
    }
}
