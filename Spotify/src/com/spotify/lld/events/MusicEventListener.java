package com.spotify.lld.events;

/**
 * Observer callback for {@link MusicEvent}s.
 * <p>
 * Why: allows NotificationService (and future recommenders/analytics) to
 * subscribe without the publisher knowing concrete consumers.
 * <p>
 * Logic: {@link #onEvent} is invoked asynchronously by {@link AsyncEventBus}.
 */
@FunctionalInterface
public interface MusicEventListener {
    /** Handle one published event (should be fast or offload heavy work). */
    void onEvent(MusicEvent event);
}
