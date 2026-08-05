package com.spotify.lld.recommendation;

import java.time.Instant;

/**
 * One user–track interaction feeding the recommendation pipeline.
 * <p>
 * Why: recommendations are event-driven; without PLAY/SKIP/LIKE/etc. there is
 * nothing to rank.
 * <p>
 * Logic: immutable record of who, which track, what action, how long they
 * listened, and when. Duration informs SKIP weighting in the engine.
 */
public class ListenEvent {
    private final String userId;
    private final String trackId;
    private final ListenEventType type;
    /** How long the user listened before the action (esp. useful for SKIP). */
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
