package com.spotify.lld.events;

import java.time.Instant;

public class MusicEvent {
    public enum Type {
        NEW_RELEASE, FRIEND_LIKED, PLAYLIST_UPDATED, USER_FOLLOWED, ARTIST_ALERT
    }

    public final Type type;
    public final String actorId;
    public final String payload;
    public final Instant timestamp;

    public MusicEvent(Type type, String actorId, String payload) {
        this.type = type;
        this.actorId = actorId;
        this.payload = payload;
        this.timestamp = Instant.now();
    }
}
