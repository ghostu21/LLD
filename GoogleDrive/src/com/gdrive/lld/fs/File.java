package com.gdrive.lld.fs;

import com.gdrive.lld.account.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Leaf in the composite tree. Content is guarded by a read/write lock with timeout.
 * <p>
 * Why: many readers, one writer; {@code tryLock} prevents waiting forever.
 * Versions use {@link AtomicLong} so two writers never mint the same id.
 */
public final class File extends DriveNode {
    public static volatile long WRITE_LOCK_TIMEOUT_MS = 5_000L;

    private final User owner;
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final List<FileVersion> versions = new CopyOnWriteArrayList<>();
    private final List<String> comments = new CopyOnWriteArrayList<>();
    private final AtomicLong versionCounter = new AtomicLong();
    private String content = "";

    public File(String name, Folder parentFolder, User owner) {
        super(name, parentFolder);
        this.owner = owner;
    }

    public User getOwner() {
        return owner;
    }

    public int sizeBytes() {
        return content.getBytes().length;
    }

    public void writeContent(String content) throws InterruptedException {
        if (!rwLock.writeLock().tryLock(WRITE_LOCK_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
            throw new RuntimeException("Could not acquire write lock on file: " + getName());
        }
        try {
            this.content = content;
            long id = versionCounter.incrementAndGet();
            versions.add(new FileVersion(id, content, System.currentTimeMillis()));
            publish("File updated: " + getName() + " v" + id);
        } finally {
            rwLock.writeLock().unlock();
        }
    }

    public String readContent() {
        rwLock.readLock().lock();
        try {
            return content;
        } finally {
            rwLock.readLock().unlock();
        }
    }

    public void revertTo(long versionId) throws InterruptedException {
        FileVersion target = versions.stream()
                .filter(v -> v.getVersionId() == versionId)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown version " + versionId));
        writeContent(target.getContent());
    }

    public List<FileVersion> getVersions() {
        return Collections.unmodifiableList(new ArrayList<>(versions));
    }

    public void addComment(String comment) {
        comments.add(comment);
        publish("Comment on " + getName() + ": " + comment);
    }

    public List<String> getComments() {
        return Collections.unmodifiableList(comments);
    }

    /**
     * Held by concurrency demos so a second writer hits tryLock timeout.
     */
    public boolean tryHoldWriteLock(long millis) throws InterruptedException {
        return rwLock.writeLock().tryLock(millis, TimeUnit.MILLISECONDS);
    }

    public void releaseWriteLock() {
        rwLock.writeLock().unlock();
    }

    @Override
    public void add(FileSystemComponent component) {
        throw new UnsupportedOperationException("Cannot add to a file.");
    }

    @Override
    public void remove(FileSystemComponent component) {
        throw new UnsupportedOperationException("Cannot remove from a file.");
    }

    @Override
    public FileSystemComponent getChild(String name) {
        throw new UnsupportedOperationException("Files do not contain children.");
    }

    @Override
    public void display(String indent) {
        System.out.println(indent + "File: " + getName());
    }
}
