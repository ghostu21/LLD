package com.spotify.lld.recommendation;

import java.time.Instant;

public class ListenEvent {
    private final String userId;
    private final String trackId;
    private final ListenEventType type;
    private final long listenDurationMs;
    private final Instant timestamp;

    public ListenEvent(String userId, String trackId,
                       ListenEventType type, long listenDurationMs) {
        this.userId = userId;
        this.trackId = trackId;
        this.type = type;
        this.listenDurationMs = listenDurationMs;
        this.timestamp = Instant.now();
    }

    public String getUserId() { return userId; }
    public String getTrackId() { return trackId; }
    public ListenEventType getType() { return type; }
    public long getListenDurationMs() { return listenDurationMs; }
    public Instant getTimestamp() { return timestamp; }
}
