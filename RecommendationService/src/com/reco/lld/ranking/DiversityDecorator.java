package com.reco.lld.ranking;

import com.reco.lld.catalog.Category;
import com.reco.lld.catalog.Item;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Decorator: cap how many consecutive/total items share a category.
 * <p>
 * Why: a strong electronics affinity would otherwise return a monotone
 * slate — diversity is a product requirement, not a ranker detail.
 */
public class DiversityDecorator implements RankingStrategy {
    private final RankingStrategy inner;
    private final int maxPerCategory;

    public DiversityDecorator(RankingStrategy inner, int maxPerCategory) {
        this.inner = inner;
        this.maxPerCategory = maxPerCategory;
    }

    @Override
    public String name() {
        return inner.name() + "+diversity";
    }

    @Override
    public List<ScoredItem> rank(RankingContext context) {
        List<ScoredItem> ranked = inner.rank(context);
        Map<Category, Integer> counts = new EnumMap<>(Category.class);
        List<ScoredItem> diversified = new ArrayList<>();
        List<ScoredItem> overflow = new ArrayList<>();
        for (ScoredItem scored : ranked) {
            Item item = context.getCatalog().find(scored.getItemId());
            if (item == null) continue;
            int used = counts.getOrDefault(item.getCategory(), 0);
            if (used < maxPerCategory) {
                diversified.add(scored);
                counts.put(item.getCategory(), used + 1);
            } else {
                overflow.add(scored);
            }
        }
        diversified.addAll(overflow);
        return diversified;
    }
}
