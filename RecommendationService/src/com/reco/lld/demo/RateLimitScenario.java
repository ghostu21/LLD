package com.reco.lld.demo;

import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;
import com.reco.lld.security.RateLimitExceededException;
import com.reco.lld.security.RateLimiter;
import com.reco.lld.service.RecommendationFacade;
import com.reco.lld.experiment.ExperimentAssigner;
import com.reco.lld.pipeline.FilterChain;
import com.reco.lld.profile.ProfileService;
import com.reco.lld.ranking.RankingStrategyFactory;

/**
 * Tight per-user budget — excess recommend calls are rejected.
 */
public class RateLimitScenario implements FeatureScenario {
    @Override
    public void run(DemoFixtures fx) {
        System.out.println("-- rate limit --");
        RecommendationFacade tight = new RecommendationFacade(
                fx.catalog,
                new ProfileService(fx.interactions.getStore(), fx.catalog),
                fx.interactions,
                new RankingStrategyFactory(),
                FilterChain.defaultChain(),
                new ExperimentAssigner(),
                new RateLimiter(2, 60_000),
                fx.reco.getCache(),
                fx.eventBus);
        int allowed = 0;
        int blocked = 0;
        for (int i = 0; i < 4; i++) {
            try {
                tight.recommend(RecommendationRequest.builder()
                        .actor(fx.bob)
                        .placement(Placement.HOME)
                        .limit(3)
                        .build());
                allowed++;
            } catch (RateLimitExceededException e) {
                blocked++;
            }
        }
        System.out.println("Allowed=" + allowed + " blocked=" + blocked);
    }
}
