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
 * Similar-items for PDP: tag/category overlap + co-purchase with the seed.
 * <p>
 * Why: product-detail should not dump global popularity; it should stay
 * neighborhood of the item the shopper is already looking at.
 */
public class SimilarItemsStrategy implements RankingStrategy {

    @Override
    public String name() {
        return "similar-items";
    }

    @Override
    public List<ScoredItem> rank(RankingContext context) {
        Item seed = context.getSeedItem();
        if (seed == null) return List.of();

        Map<String, Double> coWithSeed = new HashMap<>();
        Map<String, java.util.Set<String>> baskets = new HashMap<>();
        for (Interaction interaction : context.getInteractions().all()) {
            if (interaction.getType() != InteractionType.PURCHASE) continue;
            baskets.computeIfAbsent(interaction.getUserId(), k -> new java.util.HashSet<>())
                    .add(interaction.getItemId());
        }
        for (java.util.Set<String> basket : baskets.values()) {
            if (!basket.contains(seed.getItemId())) continue;
            for (String other : basket) {
                if (!other.equals(seed.getItemId())) {
                    coWithSeed.merge(other, 1.0, Double::sum);
                }
            }
        }

        List<ScoredItem> out = new ArrayList<>();
        for (Item item : context.getCatalog().all()) {
            if (item.getItemId().equals(seed.getItemId())) continue;
            double content = 0;
            if (item.getCategory() == seed.getCategory()) content += 3;
            for (String tag : item.getTags()) {
                if (seed.getTags().contains(tag)) content += 2;
            }
            double co = coWithSeed.getOrDefault(item.getItemId(), 0.0);
            out.add(new ScoredItem(item.getItemId(), content + co * 4, "SIMILAR"));
        }
        out.sort(Comparator.comparingDouble(ScoredItem::getScore).reversed());
        return out;
    }
}
