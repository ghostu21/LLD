package com.spotify.lld.catalog;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * In-memory song store plus indexed search facade.
 * <p>
 * Why: scanning a giant {@code List<Song>} is O(n) and fails at catalog scale.
 * This class keeps authoritative song records and delegates lookup to Tries.
 * <p>
 * Logic: {@link #addSong} writes the song map and inserts title/artist into
 * separate {@link CatalogSearchIndex} instances; search methods resolve ids
 * back to {@link Song} objects.
 */
public class MusicCatalog {
    /** Source of truth: trackId → Song. */
    private final Map<String, Song> songById = new ConcurrentHashMap<>();
    /** Suffix-trie index over song titles for partial/prefix match. */
    private final CatalogSearchIndex titleIndex = new CatalogSearchIndex();
    /** Suffix-trie index over artist names. */
    private final CatalogSearchIndex artistIndex = new CatalogSearchIndex();

    /**
     * Registers a song and indexes its title and artist text.
     * Logic: put by id, then insert into both Tries so search stays consistent.
     */
    public void addSong(Song song) {
        songById.put(song.getId(), song);
        titleIndex.insert(song.getTitle(), song.getId());
        artistIndex.insert(song.getArtist(), song.getId());
    }

    /**
     * Partial/prefix title search via Trie, then hydrate Song objects.
     * Logic: index returns track ids → map lookup → drop nulls (deleted races).
     */
    public List<Song> searchByTitle(String query) {
        return titleIndex.search(query).stream()
                .map(songById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /** Same as {@link #searchByTitle} but against the artist index. */
    public List<Song> searchByArtist(String query) {
        return artistIndex.search(query).stream()
                .map(songById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
