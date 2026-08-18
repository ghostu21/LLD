package com.reco.lld.demo;

import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;
import com.reco.lld.request.RecommendationResponse;

/**
 * Members with history get personalized (hybrid or content) slates.
 */
public class PersonalizeScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("-- personalize --");
        RecommendationResponse aliceHome = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.alice)
                .placement(Placement.HOME)
                .limit(5)
                .build());
        System.out.println("Alice HOME bucket=" + aliceHome.getBucket()
                + " strategy=" + aliceHome.getStrategyName());
        aliceHome.getItems().forEach(i -> System.out.println("  " + i));

        RecommendationResponse bobHome = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.bob)
                .placement(Placement.HOME)
                .limit(5)
                .build());
        System.out.println("Bob HOME bucket=" + bobHome.getBucket()
                + " strategy=" + bobHome.getStrategyName());
        bobHome.getItems().forEach(i -> System.out.println("  " + i));

        RecommendationResponse charlieHome = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.charlie)
                .placement(Placement.HOME)
                .limit(5)
                .build());
        System.out.println("Charlie HOME bucket=" + charlieHome.getBucket()
                + " (CONTROL = safer popularity/content, TREATMENT = +collaborative)");
        charlieHome.getItems().forEach(i -> System.out.println("  " + i));
    }
}
