package com.spotify.lld.demo;

import com.spotify.lld.playlist.Playlist;
import com.spotify.lld.playlist.PlaylistVisibility;
import com.spotify.lld.social.PlaylistAccessControl;
import com.spotify.lld.social.SocialGraph;

public class SocialGraphScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Social Graph & Access Control ---");
        SocialGraph graph = new SocialGraph();
        graph.follow(fx.bob.getUserId(), fx.alice.getUserId());

        Playlist secret = fx.alice.createPlaylist("Secret Mix", PlaylistVisibility.PRIVATE);
        Playlist forFans = fx.alice.createPlaylist("For Fans", PlaylistVisibility.FOLLOWERS_ONLY);
        Playlist publicList = fx.alice.createPlaylist("Hits", PlaylistVisibility.PUBLIC);
        secret.addSong(fx.song1);
        forFans.addSong(fx.song2);
        publicList.addSong(fx.song3);

        PlaylistAccessControl acl = new PlaylistAccessControl(graph);
        System.out.println("Bob → PRIVATE:        " + acl.canView(fx.bob.getUserId(), secret));
        System.out.println("Bob → FOLLOWERS_ONLY: " + acl.canView(fx.bob.getUserId(), forFans));
        System.out.println("Bob → PUBLIC:         " + acl.canView(fx.bob.getUserId(), publicList));
        System.out.println("Alice → her PRIVATE:  " + acl.canView(fx.alice.getUserId(), secret));
        System.out.println("Bob follows Alice:    " + graph.isFollowing(fx.bob.getUserId(), fx.alice.getUserId()));
    }
}
