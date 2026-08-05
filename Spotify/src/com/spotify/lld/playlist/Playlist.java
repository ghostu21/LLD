package com.spotify.lld.playlist;

import com.spotify.lld.catalog.Music;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class Playlist implements Music {
    private final String name;
    private final String ownerId;
    private PlaylistVisibility visibility;
    private final List<Music> songs = new CopyOnWriteArrayList<>();
    private static final int MAX_NESTING_DEPTH = 10;

    public Playlist(String name, String ownerId) {
        this(name, ownerId, PlaylistVisibility.PRIVATE);
    }

    public Playlist(String name, String ownerId, PlaylistVisibility visibility) {
        this.name = name;
        this.ownerId = ownerId;
        this.visibility = visibility;
    }

    public String getName() { return name; }
    public String getOwnerId() { return ownerId; }
    public PlaylistVisibility getVisibility() { return visibility; }
    public void setVisibility(PlaylistVisibility visibility) { this.visibility = visibility; }
    public List<Music> getSongs() { return List.copyOf(songs); }

    public void addSong(Music song) {
        if (song == this) {
            throw new IllegalArgumentException("A playlist cannot contain itself.");
        }
        if (song instanceof Playlist && wouldCreateCycle((Playlist) song)) {
            throw new IllegalArgumentException("Adding this playlist would create a circular reference.");
        }
        songs.add(song);
    }

    private boolean wouldCreateCycle(Playlist candidate) {
        return wouldCreateCycle(candidate, new HashSet<>());
    }

    private boolean wouldCreateCycle(Playlist candidate, Set<Playlist> visited) {
        if (candidate == this) return true;
        if (!visited.add(candidate)) return false;
        for (Music item : candidate.songs) {
            if (item instanceof Playlist pl && wouldCreateCycle(pl, visited)) {
                return true;
            }
        }
        return false;
    }

    public void removeSong(Music song) {
        songs.remove(song);
    }

    @Override
    public void play() {
        play(new HashSet<>(), 0);
    }

    private void play(Set<Playlist> visited, int depth) {
        if (depth > MAX_NESTING_DEPTH) {
            System.err.println("Max nesting depth reached, skipping: " + name);
            return;
        }
        if (!visited.add(this)) {
            System.err.println("Cycle detected, skipping: " + name);
            return;
        }
        System.out.println("Playing playlist: " + name);
        for (Music item : songs) {
            if (item instanceof Playlist pl) {
                pl.play(visited, depth + 1);
            } else {
                item.play();
            }
        }
    }
}
