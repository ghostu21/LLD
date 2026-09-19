package com.gdrive.lld.demo;

import com.gdrive.lld.fs.File;
import com.gdrive.lld.fs.FileSystemFactory;
import com.gdrive.lld.fs.Folder;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Write lock timeout: one holder, second writer fails instead of waiting forever.
 */
public final class ConcurrencyScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("--- File write lock timeout ---");
        Folder docs = FileSystemFactory.createFolder("Docs", fx.root, fx.owner);
        File notes = FileSystemFactory.createFile("notes.txt", docs, fx.owner);
        long previous = File.WRITE_LOCK_TIMEOUT_MS;
        File.WRITE_LOCK_TIMEOUT_MS = 200L;
        notes.tryHoldWriteLock(1_000);
        AtomicInteger failures = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(2);
        CountDownLatch done = new CountDownLatch(1);
        try {
            pool.submit(() -> {
                try {
                    notes.writeContent("from-other-thread");
                } catch (RuntimeException e) {
                    failures.incrementAndGet();
                    System.out.println("Expected lock failure: " + e.getMessage());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
            done.await();
            notes.releaseWriteLock();
            System.out.println("Timed-out writers: " + failures.get() + " (expect 1)");
        } finally {
            File.WRITE_LOCK_TIMEOUT_MS = previous;
            pool.shutdownNow();
        }
    }
}
