package com.reco.lld.pipeline;

import com.reco.lld.ranking.RankingContext;
import com.reco.lld.ranking.ScoredItem;

/**
 * Honors explicit hide/dislike plus request-level exclusions (seen items).
 */
public class BlockedItemFilter implements RecommendationFilter {
    @Override
    public boolean keep(ScoredItem item, RankingContext context) {
        if (context.getProfile().getBlockedItemIds().contains(item.getItemId())) {
            return false;
        }
        return !context.getRequest().getExtraExclusions().contains(item.getItemId());
    }
}
