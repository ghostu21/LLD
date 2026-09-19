package com.gdrive.lld.fs;

/**
 * Immutable content snapshot. Safe to share across threads with no extra lock.
 */
public final class FileVersion {
    private final long versionId;
    private final String content;
    private final long timestamp;

    public FileVersion(long versionId, String content, long timestamp) {
        this.versionId = versionId;
        this.content = content;
        this.timestamp = timestamp;
    }

    public long getVersionId() {
        return versionId;
    }

    public String getContent() {
        return content;
    }

    public long getTimestamp() {
        return timestamp;
    }
}
