package com.spotify.lld.demo;

import com.spotify.lld.playlist.Playlist;
import com.spotify.lld.playlist.PlaylistVisibility;

/**
 * Demo: nested playlists with cycle + self-add rejection, then safe play().
 * <p>
 * Interview angle: Composite without cycle/depth guards → StackOverflowError.
 */
public class PlaylistScenario implements FeatureScenario {
    /**
     * Builds Road Trip → Rock Mix nest, tries illegal cycle/self-add, then plays.
     */
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Playlists (thread-safe + cycle guard) ---");
        Playlist roadTrip = fx.alice.createPlaylist("Road Trip", PlaylistVisibility.PUBLIC);
        Playlist rockMix = fx.alice.createPlaylist("Rock Mix");

        roadTrip.addSong(fx.song1);
        roadTrip.addSong(fx.song2);
        rockMix.addSong(fx.song1);
        roadTrip.addSong(rockMix);

        try {
            rockMix.addSong(roadTrip);
            System.out.println("ERROR: cycle was not detected");
        } catch (IllegalArgumentException e) {
            System.out.println("Cycle blocked: " + e.getMessage());
        }

        try {
            roadTrip.addSong(roadTrip);
            System.out.println("ERROR: self-add was not detected");
        } catch (IllegalArgumentException e) {
            System.out.println("Self-add blocked: " + e.getMessage());
        }

        roadTrip.play();
    }
}
