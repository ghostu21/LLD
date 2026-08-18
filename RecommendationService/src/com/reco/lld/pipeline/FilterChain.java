package com.reco.lld.pipeline;

import com.reco.lld.ranking.RankingContext;
import com.reco.lld.ranking.ScoredItem;

import java.util.ArrayList;
import java.util.List;

/**
 * Chain of Responsibility: each filter may drop a candidate.
 * <p>
 * Why: eligibility, privacy-adjacent blocks, and purchase filters are
 * independent policies — composing them as a chain avoids a 40-line if.
 */
public class FilterChain {
    private final List<RecommendationFilter> filters;

    public FilterChain(List<RecommendationFilter> filters) {
        this.filters = List.copyOf(filters);
    }

    public static FilterChain defaultChain() {
        return new FilterChain(List.of(
                new EligibilityFilter(),
                new SeedItemFilter(),
                new BlockedItemFilter(),
                new AlreadyConsumedFilter()));
    }

    public List<ScoredItem> apply(List<ScoredItem> ranked, RankingContext context) {
        List<ScoredItem> kept = new ArrayList<>();
        for (ScoredItem item : ranked) {
            if (pass(item, context)) kept.add(item);
        }
        return kept;
    }

    private boolean pass(ScoredItem item, RankingContext context) {
        for (RecommendationFilter filter : filters) {
            if (!filter.keep(item, context)) return false;
        }
        return true;
    }
}
