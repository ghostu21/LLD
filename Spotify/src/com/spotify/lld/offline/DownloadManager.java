package com.spotify.lld.offline;

import com.spotify.lld.catalog.Song;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Manages offline downloads with DRM-like constraints (demo stubs for crypto).
 * <p>
 * Why: offline ≠ "save file". Real products require encryption, device binding,
 * and time-bound licenses (e.g. re-auth every ~30 days).
 * <p>
 * Logic: {@link #download} encrypts bytes, records path + device + 30-day expiry
 * + license token; {@link #getPlayableTrack} returns a track only if device and
 * expiry still allow playback.
 */
public class DownloadManager {
    private static final int MAX_OFFLINE_PER_USER = 10_000;
    /** userId → list of offline track records. */
    private final Map<String, List<OfflineTrack>> userTracks = new ConcurrentHashMap<>();

    /**
     * Downloads a song for offline use on a specific device.
     * Logic: enforce per-user cap → encrypt → fake disk path → store OfflineTrack
     * with expiry = now + 30 days and the provided license token.
     */
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

    /**
     * Finds a playable offline copy for this user/song/device.
     * Logic: filter by songId then {@link OfflineTrack#isPlayable(deviceId)}.
     */
    public Optional<OfflineTrack> getPlayableTrack(String userId, String songId, String deviceId) {
        return userTracks.getOrDefault(userId, List.of())
                .stream()
                .filter(t -> t.getSong().getId().equals(songId))
                .filter(t -> t.isPlayable(deviceId))
                .findFirst();
    }

    /**
     * Stub encryption. In production: AES-256-GCM with a per-user key in a KMS.
     */
    private byte[] encrypt(byte[] data) { return data; }

    /** Stub disk write for the encrypted payload. */
    private void saveToDisk(byte[] data, String path) { /* write encrypted bytes */ }
}
