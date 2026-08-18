package com.reco.lld.service;

import com.reco.lld.account.AccessControl;
import com.reco.lld.account.User;
import com.reco.lld.cache.TtlCache;
import com.reco.lld.catalog.Catalog;
import com.reco.lld.catalog.Item;
import com.reco.lld.events.AsyncEventBus;
import com.reco.lld.events.RecoEvent;
import com.reco.lld.events.RecoEventType;
import com.reco.lld.experiment.ExperimentAssigner;
import com.reco.lld.experiment.ExperimentBucket;
import com.reco.lld.pipeline.FilterChain;
import com.reco.lld.profile.ProfileService;
import com.reco.lld.profile.UserProfile;
import com.reco.lld.ranking.DiversityDecorator;
import com.reco.lld.ranking.RankingContext;
import com.reco.lld.ranking.RankingStrategy;
import com.reco.lld.ranking.RankingStrategyFactory;
import com.reco.lld.ranking.ScoredItem;
import com.reco.lld.request.RecommendationRequest;
import com.reco.lld.request.RecommendationResponse;
import com.reco.lld.request.RecommendedItem;
import com.reco.lld.security.RateLimiter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Facade for the recommend API.
 * <p>
 * Why: callers should not wire rate limit → authz → cache → factory →
 * filters → diversity themselves. This is the single entry that enforces
 * security before any ranking work.
 * <p>
 * Logic: rate-limit actor → IDOR check (actor vs target) → cache →
 * build profile → factory strategy → filter chain → diversity → truncate
 * → cache → async impression event.
 */
public class RecommendationFacade {
    private final Catalog catalog;
    private final ProfileService profiles;
    private final InteractionService interactions;
    private final RankingStrategyFactory factory;
    private final FilterChain filters;
    private final ExperimentAssigner experiments;
    private final RateLimiter rateLimiter;
    private final TtlCache<RecommendationResponse> cache;
    private final AsyncEventBus eventBus;

    public RecommendationFacade(Catalog catalog, ProfileService profiles,
                                InteractionService interactions, RankingStrategyFactory factory,
                                FilterChain filters, ExperimentAssigner experiments,
                                RateLimiter rateLimiter, TtlCache<RecommendationResponse> cache,
                                AsyncEventBus eventBus) {
        this.catalog = catalog;
        this.profiles = profiles;
        this.interactions = interactions;
        this.factory = factory;
        this.filters = filters;
        this.experiments = experiments;
        this.rateLimiter = rateLimiter;
        this.cache = cache;
        this.eventBus = eventBus;
    }

    public TtlCache<RecommendationResponse> getCache() {
        return cache;
    }

    public RecommendationResponse recommend(RecommendationRequest request) {
        User actor = request.getActor();
        rateLimiter.acquire(actor.getUserId());
        AccessControl.requireRecommendationsFor(actor, request.getTargetUserId());

        String cacheKey = cacheKey(request);
        RecommendationResponse cached = cache.get(cacheKey);
        if (cached != null) {
            return new RecommendationResponse(cached.getRequestId(), cached.getItems(),
                    cached.getStrategyName(), cached.getBucket(), true);
        }

        UserProfile profile = profiles.build(request.getTargetUserId());
        ExperimentBucket bucket = experiments.assign(request.getTargetUserId());
        Item seed = request.getSeedItemId() == null ? null : catalog.require(request.getSeedItemId());

        RankingContext context = new RankingContext(request, profile, catalog,
                interactions.getStore(), seed);
        RankingStrategy strategy = new DiversityDecorator(
                factory.create(request.getPlacement(), bucket, profile), 2);

        List<ScoredItem> ranked = filters.apply(strategy.rank(context), context);
        int limit = Math.min(request.getLimit(), ranked.size());
        List<RecommendedItem> items = new ArrayList<>();
        for (int i = 0; i < limit; i++) {
            ScoredItem scored = ranked.get(i);
            Item item = catalog.require(scored.getItemId());
            items.add(new RecommendedItem(item.getItemId(), item.getTitle(),
                    scored.getScore(), scored.getReasonCode()));
        }

        RecommendationResponse response = new RecommendationResponse(
                UUID.randomUUID().toString(), items, strategy.name(), bucket, false);
        cache.put(cacheKey, response);
        eventBus.publish(new RecoEvent(RecoEventType.RECS_GENERATED, request.getTargetUserId(),
                null, "size=" + items.size() + " strategy=" + strategy.name()));
        return response;
    }

    private static String cacheKey(RecommendationRequest request) {
        return request.getTargetUserId() + "|" + request.getPlacement() + "|"
                + request.getSeedItemId() + "|" + request.getLimit();
    }
}
