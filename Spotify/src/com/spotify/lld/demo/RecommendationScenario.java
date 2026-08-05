package com.spotify.lld.demo;

import com.spotify.lld.recommendation.ListenEvent;
import com.spotify.lld.recommendation.ListenEventType;
import com.spotify.lld.recommendation.RecommendationEngine;

import java.util.List;

/**
 * Demo: event-driven affinity (PLAY/LIKE/REPEAT/SKIP) → ranked recommendations.
 * <p>
 * Interview angle: recommendations are an event stream, not a method stub.
 */
public class RecommendationScenario implements FeatureScenario {
    /** Feeds Alice listen events; asserts top-1 is the heavily liked Queen track. */
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Recommendations (event-driven) ---");
        RecommendationEngine engine = new RecommendationEngine();
        String uid = fx.alice.getUserId();

        engine.onEvent(new ListenEvent(uid, fx.song1.getId(), ListenEventType.PLAY, 180_000));
        engine.onEvent(new ListenEvent(uid, fx.song1.getId(), ListenEventType.LIKE, 0));
        engine.onEvent(new ListenEvent(uid, fx.song1.getId(), ListenEventType.REPEAT, 200_000));
        engine.onEvent(new ListenEvent(uid, fx.song2.getId(), ListenEventType.PLAY, 60_000));
        engine.onEvent(new ListenEvent(uid, fx.song3.getId(), ListenEventType.SKIP, 5_000));

        List<String> top = engine.recommend(uid, 3);
        System.out.println("Top recommendations for Alice: " + top.size() + " track(s)");
        System.out.println("Top-1 is Queen (highest affinity): "
                + top.get(0).equals(fx.song1.getId()));
    }
}
