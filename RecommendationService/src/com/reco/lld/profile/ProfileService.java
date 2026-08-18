package com.reco.lld.profile;

import com.reco.lld.catalog.Catalog;
import com.reco.lld.catalog.Category;
import com.reco.lld.catalog.Item;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builds a {@link UserProfile} from the interaction log + catalog.
 * <p>
 * Why: keeps aggregation in one place so every strategy sees the same
 * affinities and block lists (no ranker re-implements hide/purchase rules).
 */
public class ProfileService {
    private final InteractionStore interactions;
    private final Catalog catalog;

    public ProfileService(InteractionStore interactions, Catalog catalog) {
        this.interactions = interactions;
        this.catalog = catalog;
    }

    public UserProfile build(String userId) {
        Map<Category, Double> categories = new EnumMap<>(Category.class);
        Map<String, Double> tags = new HashMap<>();
        Set<String> purchased = new HashSet<>();
        Set<String> blocked = new HashSet<>();
        int positive = 0;

        for (Interaction interaction : interactions.forUser(userId)) {
            Item item = catalog.find(interaction.getItemId());
            if (item == null) continue;

            if (interaction.getType().isBlock()) {
                blocked.add(item.getItemId());
                continue;
            }
            if (interaction.getType() == InteractionType.PURCHASE) {
                purchased.add(item.getItemId());
            }
            int w = interaction.getType().getWeight();
            if (w > 0) {
                positive++;
                categories.merge(item.getCategory(), (double) w, Double::sum);
                for (String tag : item.getTags()) {
                    tags.merge(tag, (double) w, Double::sum);
                }
            }
        }
        return new UserProfile(userId, categories, tags, purchased, blocked, positive);
    }
}
