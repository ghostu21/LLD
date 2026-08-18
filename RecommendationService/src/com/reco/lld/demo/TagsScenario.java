package com.reco.lld.demo;

import com.reco.lld.account.AccessDeniedException;
import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;
import com.reco.lld.request.RecommendationResponse;
import com.reco.lld.security.ValidationException;

import java.util.Set;

/**
 * User Service selected tags drive ranking even with no click history.
 */
public class TagsScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("-- user-service selected tags --");

        RecommendationResponse before = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.dana)
                .placement(Placement.HOME)
                .limit(5)
                .build());
        System.out.println("Dana before tags (cold start / popularity): " + before.getItems());

        fx.preferences.replaceSelectedTags(fx.dana, Set.of("Software", "architecture"));
        System.out.println("Dana selected tags: " + fx.preferences.selectedTags(fx.dana.getUserId()));

        RecommendationResponse after = fx.reco.recommend(RecommendationRequest.builder()
                .actor(fx.dana)
                .placement(Placement.HOME)
                .limit(5)
                .build());
        System.out.println("Dana after tags (software + architecture):");
        after.getItems().forEach(i -> System.out.println("  " + i));
        boolean booksLead = after.getItems().stream().limit(2)
                .anyMatch(i -> i.getTitle().contains("Clean Code")
                        || i.getTitle().contains("Domain-Driven"));
        System.out.println("Tag-matched books in top of slate? " + booksLead);

        try {
            fx.preferences.replaceSelectedTags(fx.guest, Set.of("software"));
        } catch (AccessDeniedException e) {
            System.out.println("Guest cannot set tags: " + e.getMessage());
        }
        try {
            fx.preferences.replaceSelectedTags(fx.dana, Set.of("software", "drop-table"));
        } catch (ValidationException e) {
            System.out.println("Unknown tag rejected: " + e.getMessage());
        }
    }
}
