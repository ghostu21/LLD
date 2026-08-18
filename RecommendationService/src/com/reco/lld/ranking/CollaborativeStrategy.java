package com.reco.lld.ranking;

import com.reco.lld.catalog.Item;
import com.reco.lld.profile.Interaction;
import com.reco.lld.profile.InteractionType;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Item–item collaborative filtering via purchase co-occurrence.
 * <p>
 * Why: "users like you also bought" without emitting those users' ids.
 * Scores are aggregated co-counts with the target user's purchases only.
 * <p>
 * Logic: for each user, take the set of PURCHASE item ids; for every pair
 * (a, b) increment co[a][b]. Score candidate c as sum of co[c][p] for
 * purchases p of the target user.
 */
public class CollaborativeStrategy implements RankingStrategy {

    @Override
    public String name() {
        return "collaborative";
    }

    @Override
    public List<ScoredItem> rank(RankingContext context) {
        Map<String, Map<String, Integer>> co = buildCooccurrence(context);
        Set<String> mine = context.getProfile().getPurchasedItemIds();
        List<ScoredItem> out = new ArrayList<>();
        for (Item item : context.getCatalog().snapshot()) {
            double score = 0;
            Map<String, Integer> neighbors = co.getOrDefault(item.getItemId(), Map.of());
            for (String purchased : mine) {
                score += neighbors.getOrDefault(purchased, 0);
            }
            out.add(new ScoredItem(item.getItemId(), score, "COLLABORATIVE"));
        }
        out.sort(Comparator.comparingDouble(ScoredItem::getScore).reversed());
        return out;
    }

    private Map<String, Map<String, Integer>> buildCooccurrence(RankingContext context) {
        Map<String, Set<String>> purchasesByUser = new HashMap<>();
        for (Interaction interaction : context.getInteractions().all()) {
            if (interaction.getType() != InteractionType.PURCHASE) continue;
            purchasesByUser.computeIfAbsent(interaction.getUserId(), k -> new HashSet<>())
                    .add(interaction.getItemId());
        }
        Map<String, Map<String, Integer>> co = new HashMap<>();
        for (Set<String> basket : purchasesByUser.values()) {
            List<String> ids = new ArrayList<>(basket);
            for (int i = 0; i < ids.size(); i++) {
                for (int j = 0; j < ids.size(); j++) {
                    if (i == j) continue;
                    co.computeIfAbsent(ids.get(i), k -> new HashMap<>())
                            .merge(ids.get(j), 1, Integer::sum);
                }
            }
        }
        return co;
    }
}
