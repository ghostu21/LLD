package com.reco.lld.ranking;

import java.util.List;

/**
 * Decorator: if the inner strategy fails or returns empty, fall back to popularity.
 * <p>
 * Why: personalization outages must not blank the homepage — fail open to
 * a safe, non-personalized slate rather than 500.
 */
public class FallbackDecorator implements RankingStrategy {
    private final RankingStrategy inner;
    private final RankingStrategy fallback;

    public FallbackDecorator(RankingStrategy inner, RankingStrategy fallback) {
        this.inner = inner;
        this.fallback = fallback;
    }

    @Override
    public String name() {
        return inner.name() + "+fallback";
    }

    @Override
    public List<ScoredItem> rank(RankingContext context) {
        try {
            List<ScoredItem> ranked = inner.rank(context);
            if (ranked != null && !ranked.isEmpty()) {
                return ranked;
            }
        } catch (RuntimeException ex) {
            System.err.println("Ranking fallback: " + ex.getMessage());
        }
        return fallback.rank(context);
    }
}
