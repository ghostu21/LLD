package com.reco.lld.demo;

import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;
import com.reco.lld.request.RecommendationResponse;
import com.reco.lld.request.RecommendedItem;

/**
 * Banned and out-of-stock SKUs never appear; purchases are excluded on HOME.
 */
public class FilterScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("-- eligibility filters --");
        RecommendationResponse slate = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.alice)
                .placement(Placement.HOME)
                .limit(20)
                .build());
        boolean banned = contains(slate, fx.bannedGag.getItemId());
        boolean oos = contains(slate, fx.outOfStock.getItemId());
        boolean purchasedPhone = contains(slate, fx.phone.getItemId());
        System.out.println("Banned item present? " + banned);
        System.out.println("Out-of-stock present? " + oos);
        System.out.println("Already-purchased phone present? " + purchasedPhone);
        System.out.println("Slate: " + slate.getItems());
    }

    private static boolean contains(RecommendationResponse slate, String itemId) {
        for (RecommendedItem item : slate.getItems()) {
            if (item.getItemId().equals(itemId)) return true;
        }
        return false;
    }
}
