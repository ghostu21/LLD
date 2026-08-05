package com.spotify.lld.catalog;

import java.util.UUID;

public class Song implements Music {
    private final String id;
    private final String title;
    private final String artist;
    private final String album;
    private final String genre;
    private final byte[] data;

    public Song(String title, String artist, String album) {
        this(title, artist, album, "Pop", new byte[16_384]);
    }

    public Song(String title, String artist, String album, String genre, byte[] data) {
        this.id = UUID.randomUUID().toString();
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.genre = genre;
        this.data = data;
    }

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
