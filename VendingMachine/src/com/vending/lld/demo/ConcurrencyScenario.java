package com.vending.lld.demo;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Two threads insert coins; machine lock + AtomicInteger cents keep the balance exact.
 */
public final class ConcurrencyScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- Concurrent coin inserts ---");
        fx.machine.selectItem("S1");
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(2);
        Runnable insert = () -> {
            try {
                start.await();
                fx.machine.insertCoin(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            } finally {
                done.countDown();
            }
        };
        pool.submit(insert);
        pool.submit(insert);
        start.countDown();
        done.await();
        pool.shutdownNow();
        System.out.println("Balance cents: " + fx.machine.payment().getBalance().cents() + " (expect 200)");
        fx.machine.completeTransaction();
    }
}
