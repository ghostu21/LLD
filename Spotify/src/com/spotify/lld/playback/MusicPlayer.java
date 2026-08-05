package com.spotify.lld.playback;

import com.spotify.lld.catalog.Music;
import com.spotify.lld.catalog.Song;
import com.spotify.lld.streaming.AudioStreamBuffer;
import com.spotify.lld.streaming.StreamingPlayer;

public class MusicPlayer {
    private final StreamingPlayer streamingPlayer = new StreamingPlayer();
    private boolean isPlaying = false;

    public void playMusic(Music music) {
        music.play();
        isPlaying = true;
    }

    public void streamSong(Song song) {
        AudioStreamBuffer buffer = new AudioStreamBuffer(song.getData());
        streamingPlayer.stream(buffer, chunk ->
                System.out.println("  [stream] decoded " + chunk.length + " bytes of " + song.getTitle()));
        isPlaying = true;
    }

    public void stopMusic() {
        streamingPlayer.stop();
        isPlaying = false;
    }

    public boolean isPlaying() { return isPlaying; }

    public void shutdown() { streamingPlayer.shutdown(); }
}
