package com.reco.lld.demo;

import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;

/**
 * Async event bus fans out RECS_GENERATED without blocking the slate.
 */
public class NotifyScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) throws Exception {
        System.out.println("-- notify (async) --");
        fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.bob)
                .placement(Placement.HOME)
                .limit(3)
                .build());
        Thread.sleep(150);
        System.out.println("Slate returned without waiting on notification I/O.");
    }
}
