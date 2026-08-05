package com.spotify.lld.playlist;

import com.spotify.lld.catalog.Music;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Composite Music node: ordered collection of songs and/or nested playlists.
 * <p>
 * Why: nested playlists are a real product feature, but naive Composite
 * allows cycles (A→B→A) and StackOverflowError on play(). Also, play and
 * edit happen concurrently, so a plain ArrayList is unsafe.
 * <p>
 * Logic:
 * <ul>
 *   <li>Store items in {@link CopyOnWriteArrayList} for concurrent iteration</li>
 *   <li>{@link #addSong} rejects self-add and cycle-creating nests</li>
 *   <li>{@link #play} walks with visited-set + depth cap as a second guard</li>
 * </ul>
 */
public class Playlist implements Music {
    private final String name;
    private final String ownerId;
    private PlaylistVisibility visibility;
    /** COW list: readers see a stable snapshot while writers copy-on-mutate. */
    private final List<Music> songs = new CopyOnWriteArrayList<>();
    /** Hard cap on nest depth to prevent stack blowups without a cycle. */
    private static final int MAX_NESTING_DEPTH = 10;

    /** Creates a PRIVATE playlist owned by {@code ownerId}. */
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

    /** Defensive copy of current items. */
    public List<Music> getSongs() { return List.copyOf(songs); }

    /**
     * Adds a song or nested playlist with cycle guards.
     * Logic: reject {@code song == this}; if nested playlist, DFS to ensure
     * adding it would not make {@code this} reachable from itself; then append.
     */
    public void addSong(Music song) {
        if (song == this) {
            throw new IllegalArgumentException("A playlist cannot contain itself.");
        }
        if (song instanceof Playlist && wouldCreateCycle((Playlist) song)) {
            throw new IllegalArgumentException("Adding this playlist would create a circular reference.");
        }
        songs.add(song);
    }

    /** Entry point for cycle check starting from a candidate child playlist. */
    private boolean wouldCreateCycle(Playlist candidate) {
        return wouldCreateCycle(candidate, new HashSet<>());
    }

    /**
     * DFS: if we can reach {@code this} by walking nested playlists under
     * {@code candidate}, adding candidate would close a cycle.
     */
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

    /** Removes a previously added item (no-op if absent). */
    public void removeSong(Music song) {
        songs.remove(song);
    }

    /** Public play entry — starts a fresh visited set at depth 0. */
    @Override
    public void play() {
        play(new HashSet<>(), 0);
    }

    /**
     * Recursive play with runtime safety nets.
     * Logic: abort if depth &gt; max or playlist already in visited;
     * otherwise print name and recurse into nested playlists / play leaves.
     */
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
