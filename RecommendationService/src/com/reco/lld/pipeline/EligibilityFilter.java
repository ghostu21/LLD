package com.reco.lld.pipeline;

import com.reco.lld.catalog.Item;
import com.reco.lld.ranking.RankingContext;
import com.reco.lld.ranking.ScoredItem;

/**
 * Drops banned / out-of-stock / unknown ids.
 * <p>
 * Why: collaborative co-occurrence can still score a listing that was
 * later banned — eligibility is a hard filter, not a ranker weight.
 */
public class EligibilityFilter implements RecommendationFilter {
    @Override
    public boolean keep(ScoredItem item, RankingContext context) {
        Item catalogItem = context.getCatalog().find(item.getItemId());
        return catalogItem != null && catalogItem.isRecommendable();
    }
}
