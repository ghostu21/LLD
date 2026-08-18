package com.reco.lld.userservice;

import com.reco.lld.catalog.Catalog;
import com.reco.lld.catalog.Item;
import com.reco.lld.catalog.TagNormalizer;
import com.reco.lld.security.ValidationException;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Validates User Service tag selections against the live catalog vocabulary.
 * <p>
 * Why: free-text tags would let a client inject features the ranker never
 * intended (and would never match items). Unknown tags are rejected, not stored.
 */
public final class TagVocabulary {
    public static final int MAX_SELECTED = 8;

    private TagVocabulary() {}

    public static Set<String> catalogTags(Catalog catalog) {
        Set<String> allowed = new LinkedHashSet<>();
        for (Item item : catalog.all()) {
            allowed.addAll(item.getTags());
        }
        return Set.copyOf(allowed);
    }

    public static Set<String> normalizeAndValidate(Catalog catalog, Set<String> requested) {
        if (requested == null) {
            throw new ValidationException("selectedTags is required");
        }
        Set<String> allowed = catalogTags(catalog);
        Set<String> out = new LinkedHashSet<>();
        for (String raw : requested) {
            String tag = TagNormalizer.normalize(raw);
            if (tag == null || !TagNormalizer.isLegalShape(tag)) {
                throw new ValidationException("Illegal tag: " + raw);
            }
            if (!allowed.contains(tag)) {
                throw new ValidationException("Tag is not in the catalog vocabulary: " + tag);
            }
            out.add(tag);
        }
        if (out.size() > MAX_SELECTED) {
            throw new ValidationException("At most " + MAX_SELECTED + " tags may be selected");
        }
        return Set.copyOf(out);
    }
}
