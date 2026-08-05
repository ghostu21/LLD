package com.spotify.lld.demo;

import com.spotify.lld.playback.PlaybackSession;
import com.spotify.lld.playback.PlayerState;
import com.spotify.lld.playback.SessionManager;

public class SessionPlaybackScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Per-Session Playback ---");
        SessionManager sessions = new SessionManager();
        PlaybackSession aliceSession = sessions.createSession(fx.alice);
        PlaybackSession bobSession = sessions.createSession(fx.bob);

        aliceSession.play(fx.song1);
        bobSession.play(fx.song2);

        System.out.println("Alice state: " + aliceSession.getState());
        System.out.println("Bob state:   " + bobSession.getState());
        System.out.println("Independent sessions: "
                + (aliceSession.getState() == PlayerState.PLAYING
                && bobSession.getState() == PlayerState.PLAYING));

        aliceSession.stop();
        System.out.println("After Alice stops — Bob still playing: "
                + (bobSession.getState() == PlayerState.PLAYING));

        bobSession.stop();
        aliceSession.getPlayer().shutdown();
        bobSession.getPlayer().shutdown();
    }
}
