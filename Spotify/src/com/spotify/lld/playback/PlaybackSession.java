package com.spotify.lld.playback;

import com.spotify.lld.auth.User;
import com.spotify.lld.catalog.Music;
import com.spotify.lld.catalog.Song;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Per-user/device playback context: owns player, state, current track, and queue.
 * <p>
 * Why: isolates control plane so pause/resume/stop only affect this session.
 * <p>
 * Logic: {@link #play} updates state + currentTrack then delegates to
 * {@link MusicPlayer} (streaming path for Song, domain play for other Music).
 * Queue supports play-next after the current item finishes or is skipped.
 */
public class PlaybackSession {
    private final String sessionId;
    private final User user;
    /** Dedicated player instance — never shared across sessions. */
    private final MusicPlayer player = new MusicPlayer();
    private PlayerState state;
    private Music currentTrack;
    /** FIFO of upcoming tracks for this session only. */
    private final Queue<Music> queue = new LinkedList<>();

    /**
     * Binds this session to a user and starts in STOPPED state.
     */
    public PlaybackSession(String sessionId, User user) {
        this.sessionId = sessionId;
        this.user = user;
        this.state = PlayerState.STOPPED;
    }

    /**
     * Starts playback of {@code track} in this session.
     * Logic: set current + PLAYING; if Song → chunked stream; else domain play().
     */
    public void play(Music track) {
        this.currentTrack = track;
        this.state = PlayerState.PLAYING;
        if (track instanceof Song song) {
            player.streamSong(song);
        } else {
            player.playMusic(track);
        }
    }

    /** Transitions PLAYING → PAUSED (no-op if not playing). */
    public void pause() {
        if (state == PlayerState.PLAYING) state = PlayerState.PAUSED;
    }

    /** Transitions PAUSED → PLAYING (no-op if not paused). */
    public void resume() {
        if (state == PlayerState.PAUSED) state = PlayerState.PLAYING;
    }

    /**
     * Stops streaming/playback and clears current track.
     * Logic: ask player to stop → state STOPPED → currentTrack null.
     */
    public void stop() {
        player.stopMusic();
        this.state = PlayerState.STOPPED;
        this.currentTrack = null;
    }

    /** Appends a track to this session's play queue. */
    public void enqueue(Music track) { queue.add(track); }

    /**
     * Plays the next queued item, or stops if the queue is empty.
     * Logic: poll queue → play(item) or stop().
     */
    public void playNext() {
        if (!queue.isEmpty()) play(queue.poll());
        else stop();
    }

    public PlayerState getState() { return state; }
    public String getSessionId() { return sessionId; }
    public User getUser() { return user; }
    public MusicPlayer getPlayer() { return player; }
}
