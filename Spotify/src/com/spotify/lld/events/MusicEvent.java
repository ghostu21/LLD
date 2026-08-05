package com.spotify.lld.events;

import java.time.Instant;

/**
 * Immutable social/notification domain event published on {@link AsyncEventBus}.
 * <p>
 * Why: decouples producers (like/follow/release actions) from consumers
 * (notifications, analytics, etc.).
 * <p>
 * Logic: carry type, actor, free-form payload, and timestamp at construction.
 */
public class MusicEvent {
    /** High-level event categories for subscription routing. */
    public enum Type {
        NEW_RELEASE, FRIEND_LIKED, PLAYLIST_UPDATED, USER_FOLLOWED, ARTIST_ALERT
    }

    public final Type type;
    /** User or artist who caused the event. */
    public final String actorId;
    /** Human-readable or structured payload (demo: plain string). */
    public final String payload;
    public final Instant timestamp;

    public MusicEvent(Type type, String actorId, String payload) {
        this.type = type;
        this.actorId = actorId;
        this.payload = payload;
        this.timestamp = Instant.now();
    }
}
