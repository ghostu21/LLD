package com.reco.lld.ranking;

import com.reco.lld.experiment.ExperimentBucket;
import com.reco.lld.profile.UserProfile;
import com.reco.lld.request.Placement;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory Method: choose a ranking strategy from placement, experiment, tags, cold-start.
 */
public class RankingStrategyFactory {
    private final PopularityStrategy popularity = new PopularityStrategy();
    private final ContentBasedStrategy content = new ContentBasedStrategy();
    private final CollaborativeStrategy collaborative = new CollaborativeStrategy();
    private final SimilarItemsStrategy similar = new SimilarItemsStrategy();
    private final SelectedTagStrategy selectedTags = new SelectedTagStrategy();

    public RankingStrategy create(Placement placement, ExperimentBucket bucket, UserProfile profile) {
        RankingStrategy core = select(placement, bucket, profile);
        return new FallbackDecorator(core, popularity);
    }

    private RankingStrategy select(Placement placement, ExperimentBucket bucket, UserProfile profile) {
        if (placement == Placement.PRODUCT_DETAIL) {
            return similar;
        }
        if (placement == Placement.CART) {
            return blendTags(new HybridRankingStrategy(List.of(
                    new HybridRankingStrategy.WeightedStrategy(similar, 0.6),
                    new HybridRankingStrategy.WeightedStrategy(content, 0.4))), profile);
        }
        if (profile.isColdStart()) {
            return popularity;
        }
        if (profile.getPositiveSignalCount() < 2 && profile.hasSelectedTags()) {
            return blendTags(popularity, profile);
        }
        if (placement == Placement.EMAIL || bucket == ExperimentBucket.CONTROL) {
            return blendTags(new HybridRankingStrategy(List.of(
                    new HybridRankingStrategy.WeightedStrategy(popularity, 0.5),
                    new HybridRankingStrategy.WeightedStrategy(content, 0.5))), profile);
        }
        return blendTags(new HybridRankingStrategy(List.of(
                new HybridRankingStrategy.WeightedStrategy(collaborative, 0.45),
                new HybridRankingStrategy.WeightedStrategy(content, 0.35),
                new HybridRankingStrategy.WeightedStrategy(popularity, 0.20))), profile);
    }

    /**
     * When the User Service has selected tags, they are a first-class signal
     * (not an afterthought weight inside content-based click affinity).
     */
    private RankingStrategy blendTags(RankingStrategy base, UserProfile profile) {
        if (!profile.hasSelectedTags()) return base;
        List<HybridRankingStrategy.WeightedStrategy> parts = new ArrayList<>();
        parts.add(new HybridRankingStrategy.WeightedStrategy(selectedTags, 0.55));
        parts.add(new HybridRankingStrategy.WeightedStrategy(base, 0.45));
        return new HybridRankingStrategy(parts);
    }
}
