package com.spotify.lld.playback;

/**
 * Finite playback states for a {@link PlaybackSession}.
 * <p>
 * Why: explicit state machine avoids ambiguous boolean flags and makes
 * pause/resume/stop transitions easy to reason about in interviews.
 * <p>
 * Logic: PLAYING ↔ PAUSED via pause/resume; STOPPED when idle or after stop.
 */
public enum PlayerState {
    PLAYING, PAUSED, STOPPED
}
