package com.spotify.lld.offline;

import com.spotify.lld.catalog.Song;

import java.time.Instant;

public class OfflineTrack {
    private final Song song;
    private final String encryptedPath;
    private final String deviceId;
    private final Instant expiresAt;
    private final String licenseToken;

    public OfflineTrack(Song song, String encryptedPath,
                        String deviceId, Instant expiresAt, String licenseToken) {
        this.song = song;
        this.encryptedPath = encryptedPath;
        this.deviceId = deviceId;
        this.expiresAt = expiresAt;
        this.licenseToken = licenseToken;
    }

    public boolean isPlayable(String currentDeviceId) {
        boolean deviceMatches = currentDeviceId.equals(deviceId);
        boolean notExpired = Instant.now().isBefore(expiresAt);
        boolean licenseValid = licenseToken != null && !licenseToken.isEmpty();
        return deviceMatches && notExpired && licenseValid;
    }

    public Song getSong() { return song; }
    public String getEncryptedPath() { return encryptedPath; }
}
