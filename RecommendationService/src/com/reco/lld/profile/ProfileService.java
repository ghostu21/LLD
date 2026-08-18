package com.reco.lld.profile;

import com.reco.lld.catalog.Catalog;
import com.reco.lld.catalog.Category;
import com.reco.lld.catalog.Item;
import com.reco.lld.userservice.UserPreferenceService;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Builds a {@link UserProfile} from interactions + User Service selected tags.
 */
public class ProfileService {
    private final InteractionStore interactions;
    private final Catalog catalog;
    private final UserPreferenceService preferences;

    public ProfileService(InteractionStore interactions, Catalog catalog,
                          UserPreferenceService preferences) {
        this.interactions = interactions;
        this.catalog = catalog;
        this.preferences = preferences;
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
        Set<String> selected = preferences.selectedTags(userId);
        return new UserProfile(userId, categories, tags, selected, purchased, blocked, positive);
    }
}
