package com.reco.lld.userservice;

import com.reco.lld.account.AccessControl;
import com.reco.lld.account.User;
import com.reco.lld.cache.TtlCache;
import com.reco.lld.catalog.Catalog;
import com.reco.lld.concurrency.GenerationClock;
import com.reco.lld.concurrency.UserScopedLock;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * User Service slice used by ranking: explicit selected tags.
 * <p>
 * Why: onboarding preferences are first-party intent and must outrank
 * (and work without) click history. Stored separately from interactions so
 * a cold-start member still gets tag-matched items, not only global popularity.
 * <p>
 * Concurrency: per-user stripe lock + generation bump + cache invalidate
 * so a recommend in flight cannot publish a pre-update slate under the new key.
 */
public class UserPreferenceService {
    private final Catalog catalog;
    private final ConcurrentHashMap<String, Set<String>> selectedByUser = new ConcurrentHashMap<>();
    private final UserScopedLock locks;
    private final GenerationClock generations;
    private final TtlCache<?> cache;

    public UserPreferenceService(Catalog catalog, UserScopedLock locks,
                                 GenerationClock generations, TtlCache<?> cache) {
        this.catalog = catalog;
        this.locks = locks;
        this.generations = generations;
        this.cache = cache;
    }

    /**
     * Replaces the caller's selected tags (idempotent snapshot, not a patch).
     */
    public void replaceSelectedTags(User actor, Set<String> tags) {
        AccessControl.requireManagePreferences(actor);
        Set<String> validated = TagVocabulary.normalizeAndValidate(catalog, tags);
        locks.run(actor.getUserId(), () -> {
            Set<String> copy = ConcurrentHashMap.newKeySet();
            copy.addAll(validated);
            selectedByUser.put(actor.getUserId(), copy);
            generations.bumpUser(actor.getUserId());
            cache.invalidatePrefix(actor.getUserId() + "|");
        });
    }

    /** Snapshot for ranking — never the live mutable set. */
    public Set<String> selectedTags(String userId) {
        Set<String> live = selectedByUser.get(userId);
        if (live == null || live.isEmpty()) return Set.of();
        return Collections.unmodifiableSet(Set.copyOf(live));
    }
}
