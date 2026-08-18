package com.reco.lld.events;

/**
 * Payload for recommendation lifecycle events.
 * <p>
 * Why: contains userId for routing notifications to the owner only —
 * listeners must not log the full profile or other users' baskets.
 */
public final class RecoEvent {
    private final RecoEventType type;
    private final String userId;
    private final String itemId;
    private final String payload;

    public RecoEvent(RecoEventType type, String userId, String itemId, String payload) {
        this.type = type;
        this.userId = userId;
        this.itemId = itemId;
        this.payload = payload;
    }

    public RecoEventType getType() { return type; }

    public String getUserId() { return userId; }

    public String getItemId() { return itemId; }

    public String getPayload() { return payload; }
}
