package com.spotify.lld.offline;

import com.spotify.lld.catalog.Song;

import java.time.Instant;

/**
 * Metadata for one offline download (not a raw open MP3 file).
 * <p>
 * Why: captures device bind, expiry, and license token required to play
 * encrypted content offline.
 * <p>
 * Logic: {@link #isPlayable} requires matching deviceId AND now &lt; expiresAt
 * AND a non-empty license token.
 */
public class OfflineTrack {
    private final Song song;
    /** Path to encrypted local payload. */
    private final String encryptedPath;
    /** Device that performed the download — other devices cannot play. */
    private final String deviceId;
    /** After this instant, offline play must fail until re-download/refresh. */
    private final Instant expiresAt;
    /** Opaque DRM/license token proving rights at download time. */
    private final String licenseToken;

    public OfflineTrack(Song song, String encryptedPath,
                        String deviceId, Instant expiresAt, String licenseToken) {
        this.song = song;
        this.encryptedPath = encryptedPath;
        this.deviceId = deviceId;
        this.expiresAt = expiresAt;
        this.licenseToken = licenseToken;
    }

    /**
     * Offline play gate for the current device.
     * All three checks must pass: device match, not expired, license present.
     */
    public boolean isPlayable(String currentDeviceId) {
        boolean deviceMatches = currentDeviceId.equals(deviceId);
        boolean notExpired = Instant.now().isBefore(expiresAt);
        boolean licenseValid = licenseToken != null && !licenseToken.isEmpty();
        return deviceMatches && notExpired && licenseValid;
    }

    public Song getSong() { return song; }
    public String getEncryptedPath() { return encryptedPath; }
}
