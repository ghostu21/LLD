package com.reco.lld.ranking;

import com.reco.lld.catalog.Item;
import com.reco.lld.profile.Interaction;
import com.reco.lld.profile.InteractionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Global popularity: weighted interaction counts across all users.
 * <p>
 * Why: cold-start and CONTROL experiment bucket need a strategy that
 * does not require a personal history.
 */
public class PopularityStrategy implements RankingStrategy {

    @Override
    public String name() {
        return "popularity";
    }

    @Override
    public List<ScoredItem> rank(RankingContext context) {
        Map<String, Double> scores = new HashMap<>();
        for (Interaction interaction : context.getInteractions().all()) {
            if (interaction.getType().getWeight() <= 0) continue;
            scores.merge(interaction.getItemId(), (double) interaction.getType().getWeight(), Double::sum);
        }
        List<ScoredItem> out = new ArrayList<>();
        for (Item item : context.getCatalog().snapshot()) {
            double s = scores.getOrDefault(item.getItemId(), 0.0);
            out.add(new ScoredItem(item.getItemId(), s, "POPULAR"));
        }
        out.sort(Comparator.comparingDouble(ScoredItem::getScore).reversed());
        return out;
    }
}
