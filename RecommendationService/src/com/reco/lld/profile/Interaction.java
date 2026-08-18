package com.reco.lld.profile;

import java.time.Instant;

/**
 * One user–item feedback event.
 * <p>
 * Why: the ranking service does not read raw click streams; it reads a
 * typed, timestamped interaction that can be aggregated into a profile.
 */
public final class Interaction {
    private final String userId;
    private final String itemId;
    private final InteractionType type;
    private final Instant at;

    public Interaction(String userId, String itemId, InteractionType type, Instant at) {
        this.userId = userId;
        this.itemId = itemId;
        this.type = type;
        this.at = at;
    }

    public String getUserId() { return userId; }

    public String getItemId() { return itemId; }

    public InteractionType getType() { return type; }

    public Instant getAt() { return at; }
}
