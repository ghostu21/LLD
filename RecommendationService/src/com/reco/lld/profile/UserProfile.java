package com.reco.lld.profile;

import com.reco.lld.catalog.Category;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

/**
 * Aggregated, non-PII view of a user for ranking.
 * <p>
 * Why: rankers should depend on affinities, selected tags, and blocked ids —
 * not email, username, or another person's identity.
 */
public final class UserProfile {
    private final String userId;
    private final Map<Category, Double> categoryAffinity;
    private final Map<String, Double> tagAffinity;
    private final Set<String> selectedTags;
    private final Set<String> purchasedItemIds;
    private final Set<String> blockedItemIds;
    private final int positiveSignalCount;

    public UserProfile(String userId,
                       Map<Category, Double> categoryAffinity,
                       Map<String, Double> tagAffinity,
                       Set<String> selectedTags,
                       Set<String> purchasedItemIds,
                       Set<String> blockedItemIds,
                       int positiveSignalCount) {
        this.userId = userId;
        this.categoryAffinity = Collections.unmodifiableMap(categoryAffinity);
        this.tagAffinity = Collections.unmodifiableMap(tagAffinity);
        this.selectedTags = Collections.unmodifiableSet(selectedTags);
        this.purchasedItemIds = Collections.unmodifiableSet(purchasedItemIds);
        this.blockedItemIds = Collections.unmodifiableSet(blockedItemIds);
        this.positiveSignalCount = positiveSignalCount;
    }

    public String getUserId() { return userId; }

    public Map<Category, Double> getCategoryAffinity() { return categoryAffinity; }

    public Map<String, Double> getTagAffinity() { return tagAffinity; }

    public Set<String> getSelectedTags() { return selectedTags; }

    public boolean hasSelectedTags() { return !selectedTags.isEmpty(); }

    public Set<String> getPurchasedItemIds() { return purchasedItemIds; }

    public Set<String> getBlockedItemIds() { return blockedItemIds; }

    public int getPositiveSignalCount() { return positiveSignalCount; }

    /**
     * True only when there is neither click/purchase history nor explicit tags.
     * Selected tags from User Service are enough to leave cold-start popularity.
     */
    public boolean isColdStart() {
        return positiveSignalCount < 2 && selectedTags.isEmpty();
    }
}
