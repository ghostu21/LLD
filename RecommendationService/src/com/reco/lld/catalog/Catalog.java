package com.reco.lld.catalog;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory product catalog used as the candidate universe.
 * <p>
 * Why: strategies never iterate a raw list owned by the controller —
 * eligibility and lookup stay consistent for ranking, filters, and admin bans.
 */
public class Catalog {
    private final ConcurrentHashMap<String, Item> items = new ConcurrentHashMap<>();

    public void add(Item item) {
        items.put(item.getItemId(), item);
    }

    public Item require(String itemId) {
        Item item = items.get(itemId);
        if (item == null) throw new IllegalArgumentException("Unknown item: " + itemId);
        return item;
    }

    public Item find(String itemId) {
        return items.get(itemId);
    }

    public Collection<Item> all() {
        return items.values();
    }

    public List<Item> activeCandidates() {
        List<Item> out = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.isRecommendable()) out.add(item);
        }
        return out;
    }
}
