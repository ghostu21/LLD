package com.spotify.lld.auth;

import com.spotify.lld.playlist.Playlist;
import com.spotify.lld.playlist.PlaylistVisibility;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User {
    private final String userId;
    private final String username;
    private final String passwordHash;
    private final String salt;
    private final String countryCode;
    private final List<Playlist> playlists = new ArrayList<>();

    public User(String username, String plainPassword, String countryCode) throws Exception {
        this.userId = UUID.randomUUID().toString();
        this.username = username;
        this.salt = PasswordUtils.generateSalt();
        this.passwordHash = PasswordUtils.hash(plainPassword, salt);
        this.countryCode = countryCode;
    }

    public boolean checkPassword(String plainPassword) throws Exception {
        return PasswordUtils.hash(plainPassword, salt).equals(passwordHash);
    }

    public Playlist createPlaylist(String name) {
        Playlist playlist = new Playlist(name, userId);
        playlists.add(playlist);
        return playlist;
    }

    public Playlist createPlaylist(String name, PlaylistVisibility visibility) {
        Playlist playlist = new Playlist(name, userId, visibility);
        playlists.add(playlist);
        return playlist;
    }

    public List<Playlist> getPlaylists() { return List.copyOf(playlists); }
    public String getUserId() { return userId; }
    public String getUsername() { return username; }
    public String getCountryCode() { return countryCode; }
}
