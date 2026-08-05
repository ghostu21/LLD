package com.spotify.lld.playback;

import com.spotify.lld.catalog.Music;
import com.spotify.lld.catalog.Song;
import com.spotify.lld.streaming.AudioStreamBuffer;
import com.spotify.lld.streaming.StreamingPlayer;

/**
 * Session-scoped player that bridges domain play and byte streaming.
 * <p>
 * Why: {@code music.play()} is domain/UI; real delivery needs chunks → buffer
 * → decoder via {@link StreamingPlayer}.
 * <p>
 * Logic: {@link #playMusic} calls the Music composite hook;
 * {@link #streamSong} wraps song bytes in {@link AudioStreamBuffer} and
 * starts async decode; {@link #stopMusic} signals the streaming worker to halt.
 */
public class MusicPlayer {
    private final StreamingPlayer streamingPlayer = new StreamingPlayer();
    private boolean isPlaying = false;

    /**
     * Domain-level play (Composite leaf/composite {@link Music#play()}).
     * Marks the player as playing without chunked streaming.
     */
    public void playMusic(Music music) {
        music.play();
        isPlaying = true;
    }

    /**
     * Streaming path for a Song.
     * Logic: build buffer from song bytes → stream with a decoder callback that
     * simulates decode work (prints chunk size in the demo).
     */
    public void streamSong(Song song) {
        AudioStreamBuffer buffer = new AudioStreamBuffer(song.getData());
        streamingPlayer.stream(buffer, chunk ->
                System.out.println("  [stream] decoded " + chunk.length + " bytes of " + song.getTitle()));
        isPlaying = true;
    }

    /** Requests the background stream loop to stop and clears playing flag. */
    public void stopMusic() {
        streamingPlayer.stop();
        isPlaying = false;
    }

    public boolean isPlaying() { return isPlaying; }

    /** Shuts down the streaming executor — call when the session ends. */
    public void shutdown() { streamingPlayer.shutdown(); }
}
