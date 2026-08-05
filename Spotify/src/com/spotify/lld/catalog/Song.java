package com.spotify.lld.catalog;

import java.util.UUID;

/**
 * Immutable catalog entry for a single track (Composite leaf).
 * <p>
 * Why: holds metadata used by search/recs/licensing plus raw {@code data}
 * bytes that the streaming layer chunks into a buffer.
 * <p>
 * Logic: id is generated once; {@link #play()} is a simple domain hook.
 * Streaming path uses {@link #getData()} via {@code AudioStreamBuffer}, not play().
 */
public class Song implements Music {
    private final String id;
    private final String title;
    private final String artist;
    private final String album;
    private final String genre;
    /** Demo audio payload (placeholder bytes) fed into the stream buffer. */
    private final byte[] data;

    /** Convenience constructor with default genre and a fixed-size byte payload. */
    public Song(String title, String artist, String album) {
        this(title, artist, album, "Pop", new byte[16_384]);
    }

    /** Full constructor — assigns a new UUID id and stores all metadata + bytes. */
    public Song(String title, String artist, String album, String genre, byte[] data) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.data = data;
    }

    /**
     * Domain playback hook (prints title/artist).
     * Not a streaming architecture — see {@code streaming} package for that.
     */
    @Override
    public void play() {
        System.out.println("Playing song: " + title + " by " + artist);
    }

    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getAlbum() { return album; }
    public String getGenre() { return genre; }
    public byte[] getData() { return data; }
}
