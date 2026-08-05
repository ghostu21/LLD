package com.spotify.lld.demo;

import com.spotify.lld.auth.User;
import com.spotify.lld.catalog.Song;

/** Shared users and songs for feature demos. */
public final class DemoFixtures {
    public final User alice;
    public final User bob;
    public final Song song1;
    public final Song song2;
    public final Song song3;

    public DemoFixtures() throws Exception {
        alice = new User("alice", "secret123", "US");
        bob = new User("bob", "pass456", "IN");
        song1 = new Song("Bohemian Rhapsody", "Queen", "A Night at the Opera", "Rock", new byte[32_768]);
        song2 = new Song("Shape of You", "Ed Sheeran", "Divide", "Pop", new byte[24_576]);
        song3 = new Song("Jai Ho", "A.R. Rahman", "Slumdog Millionaire", "Bollywood", new byte[20_480]);
    }
}
