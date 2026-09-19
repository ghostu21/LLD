package com.gdrive.lld.fs;

import com.gdrive.lld.storage.LocalStorageStrategy;
import com.gdrive.lld.storage.StorageStrategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Process-wide filesystem (Singleton via initialization-on-demand holder).
 * <p>
 * Why: one tree and one storage strategy per JVM. Moves take locks in a
 * global name/id order so A→B and B→A cannot deadlock.
 */
public final class FileSystemManager {
    private Folder root;
    private StorageStrategy storageStrategy;

    private FileSystemManager() {
        reset();
    }

    private static final class Holder {
        private static final FileSystemManager INSTANCE = new FileSystemManager();
    }

    public static FileSystemManager getInstance() {
        return Holder.INSTANCE;
    }

    public Folder getRoot() {
        return root;
    }

    public void setStorageStrategy(StorageStrategy strategy) {
        this.storageStrategy = strategy;
    }

    public StorageStrategy getStorageStrategy() {
        return storageStrategy;
    }

    /**
     * Demo isolation: rebuild an empty root between scenarios.
     */
    public void reset() {
        root = new Folder("root", null);
        storageStrategy = new LocalStorageStrategy();
    }

    /**
     * Move {@code source} into {@code destination} under ordered locks + timeout.
     */
    public void move(FileSystemComponent source, Folder destination) throws InterruptedException {
        Folder from = source.getParentFolder();
        if (from == null) {
            throw new IllegalArgumentException("Cannot move root");
        }
        List<FileSystemComponent> ordered = new ArrayList<>();
        ordered.add(source);
        ordered.add(from);
        ordered.add(destination);
        ordered.sort(Comparator.comparing(FileSystemComponent::getName)
                .thenComparing(FileSystemComponent::getId));

        List<ReentrantLock> acquired = new ArrayList<>();
        try {
            for (FileSystemComponent node : ordered) {
                ReentrantLock lock = node.getLock();
                if (!lock.tryLock(5, TimeUnit.SECONDS)) {
                    throw new RuntimeException("Could not acquire move lock on " + node.getName());
                }
                acquired.add(lock);
            }
            from.remove(source);
            destination.add(source);
        } finally {
            for (int i = acquired.size() - 1; i >= 0; i--) {
                acquired.get(i).unlock();
            }
        }
    }
}
