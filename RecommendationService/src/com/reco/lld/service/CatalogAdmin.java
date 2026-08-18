package com.reco.lld.service;

import com.reco.lld.account.AccessControl;
import com.reco.lld.account.User;
import com.reco.lld.cache.TtlCache;
import com.reco.lld.catalog.Catalog;
import com.reco.lld.catalog.ItemStatus;
import com.reco.lld.concurrency.GenerationClock;

/**
 * Admin-only catalog eligibility changes (ban / out-of-stock).
 */
public class CatalogAdmin {
    private final Catalog catalog;
    private final TtlCache<?> cache;
    private final GenerationClock generations;

    public CatalogAdmin(Catalog catalog, TtlCache<?> cache, GenerationClock generations) {
        this.catalog = catalog;
        this.cache = cache;
        this.generations = generations;
    }

    public void setStatus(User actor, String itemId, ItemStatus status) {
        AccessControl.requireModerate(actor);
        catalog.require(itemId).setStatus(status);
        generations.bumpCatalog();
        cache.clear();
    }
}
