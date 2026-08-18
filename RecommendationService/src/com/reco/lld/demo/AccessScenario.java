package com.reco.lld.demo;

import com.reco.lld.account.AccessDeniedException;
import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;

/**
 * IDOR: a member cannot fetch another member's personalized slate.
 */
public class AccessScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("-- access / IDOR --");
        try {
            fx.reco.recommend(RecommendationRequest.builder()
                    .actor(fx.alice)
                    .targetUserId(fx.bob.getUserId())
                    .placement(Placement.HOME)
                    .limit(5)
                    .build());
        } catch (AccessDeniedException e) {
            System.out.println("Alice blocked from Bob's recs: " + e.getMessage());
        }
        var adminView = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.admin)
                .targetUserId(fx.bob.getUserId())
                .placement(Placement.HOME)
                .limit(3)
                .build());
        System.out.println("Admin debug slate for Bob (items only, no Bob PII): "
                + adminView.getItems());
    }
}
