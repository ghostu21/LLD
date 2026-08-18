package com.reco.lld.catalog;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Recommendable catalog item.
 * <p>
 * Why: ranking scores items, but eligibility (status) is a hard security and
 * trust filter — banned listings must not leak into slates even if they
 * score highly from historical co-purchases.
 */
public class Item {
    private final String itemId;
    private final String title;
    private final Category category;
    private final Set<String> tags;
    private final double price;
    private ItemStatus status;

    public Item(String title, Category category, Set<String> tags, double price) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title required");
        if (price < 0) throw new IllegalArgumentException("price cannot be negative");
        this.itemId = UUID.randomUUID().toString();
        this.title = title;
        this.category = category;
        Set<String> normalized = new LinkedHashSet<>();
        if (tags != null) {
            for (String raw : tags) {
                String tag = TagNormalizer.normalize(raw);
                if (tag != null && TagNormalizer.isLegalShape(tag)) {
                    normalized.add(tag);
                }
            }
        }
        this.tags = Collections.unmodifiableSet(normalized);
        this.price = price;
        this.status = ItemStatus.ACTIVE;
    }

    public String getItemId() { return itemId; }

    public String getTitle() { return title; }

    public Category getCategory() { return category; }

    public Set<String> getTags() { return tags; }

    public double getPrice() { return price; }

    public synchronized ItemStatus getStatus() { return status; }

    public synchronized void setStatus(ItemStatus status) {
        if (status == null) throw new IllegalArgumentException("status required");
        this.status = status;
    }

    public synchronized boolean isRecommendable() {
        return status == ItemStatus.ACTIVE;
    }
}
