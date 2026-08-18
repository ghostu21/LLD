package com.reco.lld.demo;

import com.reco.lld.command.RecordInteractionCommand;
import com.reco.lld.profile.InteractionType;
import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;
import com.reco.lld.request.RecommendationResponse;

/**
 * Hide / dislike removes an item from the next slate (Command + cache bust).
 */
public class FeedbackScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("-- feedback hide --");
        RecommendationResponse before = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.alice)
                .placement(Placement.HOME)
                .limit(8)
                .build());
        String hideId = fx.tshirt.getItemId();
        System.out.println("Before hide, slate contains T-Shirt? "
                + before.getItems().stream().anyMatch(i -> i.getItemId().equals(hideId)));

        new RecordInteractionCommand(fx.interactions, fx.alice, hideId, InteractionType.HIDE).execute();

        RecommendationResponse after = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.alice)
                .placement(Placement.HOME)
                .limit(8)
                .build());
        boolean stillThere = after.getItems().stream().anyMatch(i -> i.getItemId().equals(hideId));
        System.out.println("After hide, T-Shirt in slate? " + stillThere);
    }
}
