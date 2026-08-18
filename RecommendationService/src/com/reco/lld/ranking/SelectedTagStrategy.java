package com.reco.lld.ranking;

import com.reco.lld.catalog.Item;
import com.reco.lld.profile.UserProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Scores catalog items by overlap with User Service selected tags.
 * <p>
 * Why: explicit onboarding tags are stronger intent than inferred
 * click affinity and must work with zero interaction history.
 */
public class SelectedTagStrategy implements RankingStrategy {

    @Override
    public String name() {
        return "selected-tags";
    }

    @Override
    public List<ScoredItem> rank(RankingContext context) {
        UserProfile profile = context.getProfile();
        List<ScoredItem> out = new ArrayList<>();
        for (Item item : context.getCatalog().snapshot()) {
            int overlap = 0;
            for (String tag : item.getTags()) {
                if (profile.getSelectedTags().contains(tag)) overlap++;
            }
            out.add(new ScoredItem(item.getItemId(), overlap * 5.0, "TAG"));
        }
        out.sort(Comparator.comparingDouble(ScoredItem::getScore).reversed());
        return out;
    }
}
