package com.spotify.lld.playback;

import com.spotify.lld.auth.User;
import com.spotify.lld.catalog.Music;
import com.spotify.lld.catalog.Song;

import java.util.LinkedList;
import java.util.Queue;

public class PlaybackSession {
    private final String sessionId;
    private final User user;
    private final MusicPlayer player = new MusicPlayer();
    private PlayerState state;
    private Music currentTrack;
    private final Queue<Music> queue = new LinkedList<>();

    public PlaybackSession(String sessionId, User user) {
        this.sessionId = sessionId;
        this.user = user;
        this.state = PlayerState.STOPPED;
    }

    public void play(Music track) {
        this.currentTrack = track;
        this.state = PlayerState.PLAYING;
        if (track instanceof Song song) {
            player.streamSong(song);
        } else {
            player.playMusic(track);
        }
    }

    public void pause() {
        if (state == PlayerState.PLAYING) state = PlayerState.PAUSED;
    }

    public void resume() {
        if (state == PlayerState.PAUSED) state = PlayerState.PLAYING;
    }

    public void stop() {
        player.stopMusic();
        this.state = PlayerState.STOPPED;
        this.currentTrack = null;
    }

    public void enqueue(Music track) { queue.add(track); }

    public void playNext() {
        if (!queue.isEmpty()) play(queue.poll());
        else stop();
    }

    public PlayerState getState() { return state; }
    public String getSessionId() { return sessionId; }
    public User getUser() { return user; }
    public MusicPlayer getPlayer() { return player; }
}
