package com.spotify.lld.offline;

import com.spotify.lld.catalog.Song;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class DownloadManager {
    private static final int MAX_OFFLINE_PER_USER = 10_000;
    private final Map<String, List<OfflineTrack>> userTracks = new ConcurrentHashMap<>();

    public void download(String userId, Song song, String deviceId, String licenseToken) {
        List<OfflineTrack> tracks = userTracks.computeIfAbsent(userId,
                k -> new CopyOnWriteArrayList<>());
        if (tracks.size() >= MAX_OFFLINE_PER_USER) {
            throw new IllegalStateException("Offline storage limit reached.");
        }

        byte[] encrypted = encrypt(song.getData());
        String path = "/offline/" + userId + "/" + song.getId() + ".enc";
        saveToDisk(encrypted, path);

        Instant expiry = Instant.now().plusSeconds(30L * 24 * 3600);
        tracks.add(new OfflineTrack(song, path, deviceId, expiry, licenseToken));
    }

    public Optional<OfflineTrack> getPlayableTrack(String userId, String songId, String deviceId) {
        return userTracks.getOrDefault(userId, List.of())
                .stream()
                .filter(t -> t.getSong().getId().equals(songId))
                .filter(t -> t.isPlayable(deviceId))
                .findFirst();
    }

    /** In production: AES-256-GCM with a per-user key stored in a KMS. */
    private byte[] encrypt(byte[] data) { return data; }

    private void saveToDisk(byte[] data, String path) { /* write encrypted bytes */ }
}
