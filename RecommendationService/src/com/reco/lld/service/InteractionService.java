package com.reco.lld.service;

import com.reco.lld.account.AccessControl;
import com.reco.lld.account.User;
import com.reco.lld.cache.TtlCache;
import com.reco.lld.catalog.Catalog;
import com.reco.lld.events.AsyncEventBus;
import com.reco.lld.events.RecoEvent;
import com.reco.lld.events.RecoEventType;
import com.reco.lld.profile.Interaction;
import com.reco.lld.profile.InteractionStore;
import com.reco.lld.profile.InteractionType;
import com.reco.lld.security.InputValidator;

import java.time.Instant;

/**
 * Records implicit/explicit feedback and invalidates cached slates.
 * <p>
 * Why: feedback is a write path with its own auth rules — guests cannot
 * poison another member's profile; item ids must exist in the catalog.
 */
public class InteractionService {
    private final Catalog catalog;
    private final InteractionStore store;
    private final TtlCache<?> cache;
    private final AsyncEventBus eventBus;

    public InteractionService(Catalog catalog, InteractionStore store,
                              TtlCache<?> cache, AsyncEventBus eventBus) {
        this.catalog = catalog;
        this.store = store;
        this.cache = cache;
        this.eventBus = eventBus;
    }

    public void record(User actor, String itemId, InteractionType type) {
        AccessControl.requireRecordInteraction(actor);
        InputValidator.requireItemId(itemId);
        catalog.require(itemId);
        store.append(new Interaction(actor.getUserId(), itemId, type, Instant.now()));
        cache.invalidatePrefix(actor.getUserId() + "|");
        eventBus.publish(new RecoEvent(toEventType(type), actor.getUserId(), itemId, type.name()));
    }

    /** Seed history in demos/fixtures without going through guest auth. */
    public void seed(String userId, String itemId, InteractionType type) {
        catalog.require(itemId);
        store.append(new Interaction(userId, itemId, type, Instant.now()));
    }

    public InteractionStore getStore() {
        return store;
    }

    private static RecoEventType toEventType(InteractionType type) {
        return switch (type) {
            case HIDE, DISLIKE -> RecoEventType.ITEM_HIDDEN;
            case PURCHASE -> RecoEventType.ITEM_PURCHASED;
            default -> RecoEventType.ITEM_CLICKED;
        };
    }
}
