package com.reco.lld.demo;

import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;
import com.reco.lld.request.RecommendationResponse;

/**
 * Product-detail similar-items rail around a seed SKU.
 */
public class SimilarScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("-- similar items (PDP) --");
        RecommendationResponse similar = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.alice)
                .placement(Placement.PRODUCT_DETAIL)
                .seedItemId(fx.phone.getItemId())
                .limit(4)
                .build());
        System.out.println("Similar to Smartphone X (" + similar.getStrategyName() + "):");
        similar.getItems().forEach(i -> System.out.println("  " + i));
    }
}
