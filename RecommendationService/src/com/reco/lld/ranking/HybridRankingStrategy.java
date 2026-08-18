package com.reco.lld.ranking;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Composite: weighted blend of child strategies.
 * <p>
 * Why: no single signal is enough — popularity stabilizes cold items,
 * content captures taste, collaborative captures neighbors. Weights live
 * here so the facade does not hardcode arithmetic.
 */
public class HybridRankingStrategy implements RankingStrategy {
    private final List<WeightedStrategy> parts;

    public HybridRankingStrategy(List<WeightedStrategy> parts) {
        this.parts = List.copyOf(parts);
    }

    @Override
    public String name() {
        return "hybrid";
    }

    @Override
    public List<ScoredItem> rank(RankingContext context) {
        Map<String, Double> scores = new HashMap<>();
        for (WeightedStrategy part : parts) {
            for (ScoredItem scored : part.strategy.rank(context)) {
                scores.merge(scored.getItemId(), scored.getScore() * part.weight, Double::sum);
            }
        }
        List<ScoredItem> out = new ArrayList<>();
        for (Map.Entry<String, Double> e : scores.entrySet()) {
            out.add(new ScoredItem(e.getKey(), e.getValue(), "HYBRID"));
        }
        out.sort(Comparator.comparingDouble(ScoredItem::getScore).reversed());
        return out;
    }

    public static final class WeightedStrategy {
        final RankingStrategy strategy;
        final double weight;

        public WeightedStrategy(RankingStrategy strategy, double weight) {
            this.strategy = strategy;
            this.weight = weight;
        }
    }
}
