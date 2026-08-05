package com.spotify.lld.demo;

import com.spotify.lld.auth.User;
import com.spotify.lld.catalog.Song;

/**
 * Shared sample data for all demos (Alice US, Bob IN, three songs).
 * <p>
 * Why: scenarios need consistent users/tracks so geo-license and ACL demos
 * stay readable (e.g. Bob in IN blocked from US-only Queen).
 * <p>
 * Logic: constructor creates two users with different country codes and three
 * songs with different byte payload sizes for streaming demos.
 */
public final class DemoFixtures {
    public final User alice;
    public final User bob;
    public final Song song1;
    public final Song song2;
    public final Song song3;

    /** Builds Alice (US), Bob (IN), and Queen / Ed Sheeran / A.R. Rahman tracks. */
    public DemoFixtures() throws Exception {
        alice = new User("alice", "secret123", "US");
        bob = new User("bob", "pass456", "IN");
        song1 = new Song("Bohemian Rhapsody", "Queen", "A Night at the Opera", "Rock", new byte[32_768]);
        song2 = new Song("Shape of You", "Ed Sheeran", "Divide", "Pop", new byte[24_576]);
        song3 = new Song("Jai Ho", "A.R. Rahman", "Slumdog Millionaire", "Bollywood", new byte[20_480]);
    }
}
