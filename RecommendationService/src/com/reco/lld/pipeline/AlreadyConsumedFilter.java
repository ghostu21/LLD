package com.reco.lld.pipeline;

import com.reco.lld.ranking.RankingContext;
import com.reco.lld.ranking.ScoredItem;

/**
 * Drops items the user already purchased (homepage / email).
 * PDP similar-items may still show complements the user owns — we only
 * skip the seed itself via extra exclusions.
 */
public class AlreadyConsumedFilter implements RecommendationFilter {
    @Override
    public boolean keep(ScoredItem item, RankingContext context) {
        return !context.getProfile().getPurchasedItemIds().contains(item.getItemId());
    }
}
