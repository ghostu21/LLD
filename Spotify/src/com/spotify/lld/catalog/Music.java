package com.spotify.lld.catalog;

/**
 * Composite leaf/component contract for playable catalog items.
 * <p>
 * Why: both {@link Song} and {@link com.spotify.lld.playlist.Playlist} are
 * "Music", so playlists can nest songs and other playlists (Composite pattern).
 * <p>
 * Logic: {@link #play()} is domain playback; real byte streaming is handled
 * separately by the streaming package.
 */
public interface Music {
    /** Start domain-level playback for this item (song print / playlist walk). */
    void play();
}
