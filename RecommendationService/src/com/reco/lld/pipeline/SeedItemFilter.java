package com.reco.lld.pipeline;

import com.reco.lld.ranking.RankingContext;
import com.reco.lld.ranking.ScoredItem;

/**
 * Seed item must not appear in its own similar-items rail.
 */
public class SeedItemFilter implements RecommendationFilter {
    @Override
    public boolean keep(ScoredItem item, RankingContext context) {
        if (context.getSeedItem() == null) return true;
        return !item.getItemId().equals(context.getSeedItem().getItemId());
    }
}
