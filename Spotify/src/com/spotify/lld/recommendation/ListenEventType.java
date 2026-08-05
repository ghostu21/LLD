package com.spotify.lld.recommendation;

/**
 * Explicit listen-interaction vocabulary for the recommendation engine.
 * <p>
 * Why: different actions carry different affinity weights (SHARE &gt; LIKE &gt; PLAY;
 * SKIP is negative).
 */
public enum ListenEventType {
    PLAY, SKIP, LIKE, REPEAT, ADD_TO_PLAYLIST, SHARE
}
