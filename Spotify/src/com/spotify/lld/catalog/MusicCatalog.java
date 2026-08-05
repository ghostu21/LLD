package com.spotify.lld.catalog;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class MusicCatalog {
    private final Map<String, Song> songById = new ConcurrentHashMap<>();
    private final CatalogSearchIndex titleIndex = new CatalogSearchIndex();
    private final CatalogSearchIndex artistIndex = new CatalogSearchIndex();

    public void addSong(Song song) {
        songById.put(song.getId(), song);
        titleIndex.insert(song.getTitle(), song.getId());
        artistIndex.insert(song.getArtist(), song.getId());
    }

    public List<Song> searchByTitle(String query) {
        return titleIndex.search(query).stream()
                .map(songById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public List<Song> searchByArtist(String query) {
        return artistIndex.search(query).stream()
                .map(songById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}
