package com.reco.lld.ranking;

import com.reco.lld.experiment.ExperimentBucket;
import com.reco.lld.profile.UserProfile;
import com.reco.lld.request.Placement;

import java.util.List;

/**
 * Factory Method: choose a ranking strategy from placement, experiment, cold-start.
 * <p>
 * Why: keeps the facade from growing a nested switch each time a new
 * placement or experiment arm is added.
 */
public class RankingStrategyFactory {
    private final PopularityStrategy popularity = new PopularityStrategy();
    private final ContentBasedStrategy content = new ContentBasedStrategy();
    private final CollaborativeStrategy collaborative = new CollaborativeStrategy();
    private final SimilarItemsStrategy similar = new SimilarItemsStrategy();

    public RankingStrategy create(Placement placement, ExperimentBucket bucket, UserProfile profile) {
        RankingStrategy core = select(placement, bucket, profile);
        return new FallbackDecorator(core, popularity);
    }

    private RankingStrategy select(Placement placement, ExperimentBucket bucket, UserProfile profile) {
        if (placement == Placement.PRODUCT_DETAIL) {
            return similar;
        }
        if (placement == Placement.CART) {
            return new HybridRankingStrategy(List.of(
                    new HybridRankingStrategy.WeightedStrategy(similar, 0.6),
                    new HybridRankingStrategy.WeightedStrategy(content, 0.4)));
        }
        if (placement == Placement.EMAIL || bucket == ExperimentBucket.CONTROL || profile.isColdStart()) {
            if (profile.isColdStart()) return popularity;
            return new HybridRankingStrategy(List.of(
                    new HybridRankingStrategy.WeightedStrategy(popularity, 0.5),
                    new HybridRankingStrategy.WeightedStrategy(content, 0.5)));
        }
        return new HybridRankingStrategy(List.of(
                new HybridRankingStrategy.WeightedStrategy(collaborative, 0.45),
                new HybridRankingStrategy.WeightedStrategy(content, 0.35),
                new HybridRankingStrategy.WeightedStrategy(popularity, 0.20)));
    }
}
