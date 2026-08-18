package com.reco.lld.ranking;

import com.reco.lld.catalog.Catalog;
import com.reco.lld.catalog.Item;
import com.reco.lld.profile.InteractionStore;
import com.reco.lld.profile.UserProfile;
import com.reco.lld.request.Placement;
import com.reco.lld.request.RecommendationRequest;

/**
 * Inputs shared by every ranking strategy (Template-style context object).
 */
public final class RankingContext {
    private final RecommendationRequest request;
    private final UserProfile profile;
    private final Catalog catalog;
    private final InteractionStore interactions;
    private final Item seedItem;

    public RankingContext(RecommendationRequest request, UserProfile profile,
                          Catalog catalog, InteractionStore interactions, Item seedItem) {
        this.request = request;
        this.profile = profile;
        this.catalog = catalog;
        this.interactions = interactions;
        this.seedItem = seedItem;
    }

    public RecommendationRequest getRequest() { return request; }

    public UserProfile getProfile() { return profile; }

    public Catalog getCatalog() { return catalog; }

    public InteractionStore getInteractions() { return interactions; }

    public Item getSeedItem() { return seedItem; }

    public Placement getPlacement() { return request.getPlacement(); }
}
