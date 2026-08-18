package com.reco.lld.ranking;

import com.reco.lld.catalog.Item;
import com.reco.lld.profile.UserProfile;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Content-based: score = category affinity + overlapping tags.
 * <p>
 * Why: works with a single user's history (no need for neighbors) and
 * never consults another user's identity.
 */
public class ContentBasedStrategy implements RankingStrategy {

    @Override
    public String name() {
        return "content";
    }

    @Override
    public List<ScoredItem> rank(RankingContext context) {
        UserProfile profile = context.getProfile();
        List<ScoredItem> out = new ArrayList<>();
        for (Item item : context.getCatalog().all()) {
            double cat = profile.getCategoryAffinity().getOrDefault(item.getCategory(), 0.0);
            double tag = 0;
            for (String t : item.getTags()) {
                tag += profile.getTagAffinity().getOrDefault(t, 0.0);
            }
            out.add(new ScoredItem(item.getItemId(), cat + tag, "CONTENT"));
        }
        out.sort(Comparator.comparingDouble(ScoredItem::getScore).reversed());
        return out;
    }
}
