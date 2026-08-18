package com.reco.lld.pipeline;

import com.reco.lld.ranking.RankingContext;
import com.reco.lld.ranking.ScoredItem;

/**
 * One stage in the post-ranking filter chain (Chain of Responsibility).
 */
public interface RecommendationFilter {
    boolean keep(ScoredItem item, RankingContext context);
}
