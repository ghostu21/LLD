package com.spotify.lld.auth;

import com.spotify.lld.playlist.Playlist;
import com.spotify.lld.playlist.PlaylistVisibility;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Domain model for an authenticated account.
 * <p>
 * Why: never store plaintext passwords; keep a country code for licensing
 * checks; own the user's playlist collection.
 * <p>
 * Logic: constructor generates a salt, hashes the password once, and stores
 * only hash + salt. {@link #checkPassword} re-hashes the candidate with the
 * same salt and compares. Playlist factories attach ownership to this userId.
 */
public class User {
    private final String userId;
    private final String username;
    /** One-way hash of the password; never store the raw password. */
    private final String passwordHash;
    /** Per-user random salt so identical passwords do not share hashes. */
    private final String salt;
    /** ISO country used by {@code LicenseService} geo checks. */
    private final String countryCode;
    private final List<Playlist> playlists = new ArrayList<>();

    /**
     * Creates a user: generates userId + salt, hashes {@code plainPassword},
     * and records {@code countryCode} for later license enforcement.
     */
    public User(String username, String plainPassword, String countryCode) throws Exception {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.salt = PasswordUtils.generateSalt();
        this.passwordHash = PasswordUtils.hash(plainPassword, salt);
        this.countryCode = countryCode;
    }

    /**
     * Verifies a login attempt by hashing with this user's salt and comparing
     * to the stored hash (constant-time compare would be preferred in production).
     */
    public boolean checkPassword(String plainPassword) throws Exception {
        return PasswordUtils.hash(plainPassword, salt).equals(passwordHash);
    }

    /** Creates a PRIVATE playlist owned by this user and adds it to the list. */
    public Playlist createPlaylist(String name) {
        Playlist playlist = new Playlist(name, userId);
        playlists.add(playlist);
        return playlist;
    }

    /** Creates a playlist with explicit visibility (PUBLIC / FOLLOWERS_ONLY / PRIVATE). */
    public Playlist createPlaylist(String name, PlaylistVisibility visibility) {
        Playlist playlist = new Playlist(name, userId, visibility);
        playlists.add(playlist);
        return playlist;
    }

    /** Defensive copy so callers cannot mutate the internal playlist list. */
    public List<Playlist> getPlaylists() { return List.copyOf(playlists); }

    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getCountryCode() { return countryCode; }
}
