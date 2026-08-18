package com.reco.lld.demo;

import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;
import com.reco.lld.request.RecommendationResponse;

/**
 * New / guest users get popularity, not an empty slate.
 */
public class ColdStartScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("-- cold start --");
        RecommendationResponse guestHome = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.guest)
                .placement(Placement.HOME)
                .limit(5)
                .build());
        System.out.println("Guest HOME (" + guestHome.getStrategyName() + "): " + guestHome.getItems());
    }
}
