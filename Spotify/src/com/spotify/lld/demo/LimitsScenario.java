package com.spotify.lld.demo;

import com.spotify.lld.limits.RateLimiter;
import com.spotify.lld.limits.StreamLimiter;

/**
 * Demo: CAS concurrent-stream cap + token-bucket API rate limit.
 * <p>
 * Interview angle: without limits, abuse is guaranteed.
 */
public class LimitsScenario implements FeatureScenario {
    /**
     * Acquires one stream (max=1) so second fails; bursts rate limiter to show drop.
     */
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("--- Stream Limits & Rate Limiting ---");
        StreamLimiter streams = new StreamLimiter(1);
        RateLimiter rate = new RateLimiter(5);

        String userId = fx.alice.getUserId();
        boolean first = streams.tryAcquireStream(userId);
        boolean second = streams.tryAcquireStream(userId);
        System.out.println("First stream:  " + first);
        System.out.println("Second blocked: " + !second);
        streams.releaseStream(userId);

        int allowed = 0;
        for (int i = 0; i < 12; i++) {
            if (rate.tryConsume(userId)) allowed++;
        }
        System.out.println("Rate limit: " + allowed + " / 12 calls allowed (burst ≈ 10)");
    }
}
